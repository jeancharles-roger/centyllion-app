package com.centyllion.backend

import com.centyllion.model.*
import com.zaxxer.hikari.HikariDataSource
import io.ktor.auth.jwt.JWTPrincipal
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.SqlExpressionBuilder.wrap
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*
import javax.sql.rowset.serial.SerialBlob

class FullTextSearchOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "@@")

infix fun ExpressionWithColumnType<Any>.fullTextSearch(t: String): Op<Boolean> {
    return FullTextSearchOp(this, ToTsQuery(wrap(t)))
}

class ToTsQuery<T : String?>(val expr: Expression<T>) : Function<T>(VarCharColumnType()) {
    override fun toSQL(queryBuilder: QueryBuilder): String = "to_tsquery(${expr.toSQL(queryBuilder)})"
}

class SqlData(
    type: String = "postgresql",
    host: String = "localhost",
    port: Int = 5432,
    name: String = "centyllion",
    user: String = "centyllion",
    password: String = ""
) : Data {

    // TODO create events for all access

    val url = "jdbc:$type://$host:$port/$name"

    val dataSource = HikariDataSource().apply {
        jdbcUrl = url
        username = user
        setPassword(password)
    }

    val database = Database.connect(dataSource)

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                DbMetaTable, DbUsers, DbFeaturedTable, DbAssets,
                DbDescriptionInfos, DbModelDescriptions, DbSimulationDescriptions
            )
            val version = try {
                DbMeta.all().first().version
            } catch (e: NoSuchElementException) {
                // meta doesn't exists, creates tables and insert meta version

                DbMeta.new { version = 0 }
                0
            }

            // apply migration
            migrations.dropWhile { it.to <= version }.forEach { it.update(this) }

            DbMeta.all().first().version = migrations.last().to
        }
    }

    override fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User {
        // retrieves roles from claim
        val currentRoles = principal.payload.claims["roles"]?.asList(String::class.java)?.joinToString(",")

        // find or create the user
        val user = transaction(database) {
            DbUser.find { DbUsers.keycloak eq principal.payload.subject }.firstOrNull()
        } ?: principal.payload.claims.let { claims ->
            transaction(database) {
                DbUser.new {
                    keycloak = principal.payload.subject
                    name = claims["name"]?.asString() ?: ""
                    email = claims["email"]?.asString() ?: ""
                    roles = currentRoles ?: ""
                }
            }
        }
        if (user.roles != currentRoles) {
            // updates roles for user
            transaction { user.roles = currentRoles ?: "" }
        }
        return user.toModel(true)
    }

    override fun getUser(id: String, detailed: Boolean): User? = transaction(database) {
        DbUser.findById(UUID.fromString(id))?.toModel(detailed)
    }

    override fun saveUser(user: User) {
        transaction(database) { DbUser.findById(UUID.fromString(user.id))?.fromModel(user) }
    }

    override fun publicGrainModels(offset: Int, limit: Int) = transaction(database) {
        DbModelDescription.wrapRows(DbModelDescriptions.innerJoin(DbDescriptionInfos).select {
            (DbDescriptionInfos.id eq DbModelDescriptions.info) and (DbDescriptionInfos.readAccess eq true)
        }.limit(limit, offset)).map { it.toModel() }
    }

    override fun grainModelsForUser(user: User): List<GrainModelDescription> = transaction(database) {
        val userUUID = UUID.fromString(user.id)
        DbModelDescription.wrapRows(
            DbModelDescriptions
                .innerJoin(DbDescriptionInfos)
                .select { DbDescriptionInfos.userId eq userUUID }
                .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }
    }

    override fun getGrainModel(id: String) = transaction(database) {
        DbModelDescription.findById(UUID.fromString(id))?.toModel()
    }

    override fun createGrainModel(user: User, sent: GrainModel): GrainModelDescription = transaction(database) {
        val newInfo = DbDescriptionInfo.new {
            userId = UUID.fromString(user.id)
            createdOn = DateTime.now()
            lastModifiedOn = DateTime.now()
            readAccess = false
            cloneAccess = false
        }
        DbModelDescription.new {
            info = newInfo
            model = Json.stringify(GrainModel.serializer(), sent)
            version = 0
            type = DbModelType.Grain.toString()
        }.toModel()
    }

    override fun saveGrainModel(user: User, model: GrainModelDescription) {
        transaction(database) {
            val found = DbModelDescription.findById(UUID.fromString(model.id))
            found?.fromModel(model)
            found?.info?.lastModifiedOn = DateTime.now()
        }
    }

    override fun deleteGrainModel(user: User, modelId: String) {
        transaction(database) {
            DbSimulationDescription
                .find { DbSimulationDescriptions.modelId eq UUID.fromString(modelId) }
                .forEach { deleteSimulation(it) }
            deleteGrainModel(DbModelDescription.findById(UUID.fromString(modelId)))
        }
    }

    /** Must be called inside transaction */
    private fun deleteGrainModel(model: DbModelDescription?) {
        model?.let {
            it.delete()
            it.info.delete()
        }
    }

    override fun getSimulationForModel(modelId: String): List<SimulationDescription> = transaction(database) {
        DbSimulationDescription
            .find { DbSimulationDescriptions.modelId eq UUID.fromString(modelId) }
            .map { it.toModel() }
    }

    override fun getSimulation(id: String) = transaction(database) {
        DbSimulationDescription.findById(UUID.fromString(id))?.toModel()
    }


    override fun createSimulation(user: User, modelId: String, sent: Simulation) =
        transaction(database) {
            val newInfo = DbDescriptionInfo.new {
                userId = UUID.fromString(user.id)
                createdOn = DateTime.now()
                lastModifiedOn = DateTime.now()
                readAccess = false
                cloneAccess = false
            }

            DbSimulationDescription.new {
                info = newInfo
                this.modelId = UUID.fromString(modelId)
                simulation = Json.stringify(Simulation.serializer(), sent)
                version = 0
                type = DbModelType.Grain.toString()
            }.toModel()
        }

    override fun saveSimulation(user: User, simulation: SimulationDescription) {
        // TODO thumbnails should be sent from client
        transaction(database) {
            val found = DbSimulationDescription.findById(UUID.fromString(simulation.id))
            // removes current asset if exists
            val toSave = simulation.let {
                simulation.thumbnailId?.let { deleteAsset(it) }
                // creates new asset
                val asset = getGrainModel(simulation.modelId)?.let {
                    createAsset("${simulation.simulation.name}.png", createThumbnail(it.model, simulation.simulation))
                }
                simulation.copy(thumbnailId = asset?.id)
            }
            // updates simulation
            found?.fromModel(toSave)
            found?.info?.lastModifiedOn = DateTime.now()
        }
    }

    override fun deleteSimulation(user: User, simulationId: String) {
        transaction(database) { deleteSimulation(DbSimulationDescription.findById(UUID.fromString(simulationId))) }
    }

    /** Must be called inside transaction */
    private fun deleteSimulation(simulation: DbSimulationDescription?) {
        simulation?.let {
            it.delete()
            it.info.delete()
        }
    }

    override fun getAllFeatured(offset: Int, limit: Int): List<FeaturedDescription> = transaction(database) {
        DbFeatured.all().limit(limit, offset).reversed().map { it.toModel() }
    }

    override fun getFeatured(id: String) = transaction(database) {
        DbFeatured.findById(UUID.fromString(id))?.toModel()
    }

    override fun createFeatured(
        user: User, model: GrainModelDescription, simulation: SimulationDescription, author: User
    ) = transaction(database) {
        DbFeatured.new { featuredId = UUID.fromString(simulation.id) }.toModel()
    }

    override fun deleteFeatured(user: User, featuredId: String) {
        transaction(database) {
            DbFeatured.findById(UUID.fromString(featuredId))?.delete()
        }
    }

    override fun searchSimulation(query: String, offset: Int, limit: Int): List<SimulationDescription> = transaction {
        DbSimulationDescription.wrapRows(
            DbSimulationDescriptions
                .innerJoin(DbDescriptionInfos)
                .select {
                    (DbDescriptionInfos.readAccess eq true) and
                    (DbSimulationDescriptions.searchable fullTextSearch query)
                }
                .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }
    }

    override fun searchModel(query: String, offset: Int, limit: Int): List<GrainModelDescription> = transaction {
        DbModelDescription.wrapRows(
            DbModelDescriptions
                .innerJoin(DbDescriptionInfos)
                .select {
                    (DbDescriptionInfos.readAccess eq true) and
                    (DbModelDescriptions.searchable fullTextSearch query)
                }
                .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }
    }

    override fun getAsset(id: String) = transaction(database) {
        DbAsset.findById(UUID.fromString(id))?.toModel()
    }

    override fun createAsset(name: String, data: ByteArray): Asset = transaction(database) {
        DbAsset.new {
            this.name = name
            this.content = SerialBlob(data)
        }.toModel()
    }

    override fun deleteAsset(id: String) {
        transaction(database) { DbAsset.findById(UUID.fromString(id))?.delete() }
    }
}


@file:UseExperimental(UnstableDefault::class)
package com.centyllion.backend.data

import com.centyllion.model.Asset
import com.centyllion.model.CollectionInfo
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ResultPage
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.User
import com.zaxxer.hikari.HikariDataSource
import io.ktor.auth.jwt.JWTPrincipal
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DateColumnType
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.GreaterEqOp
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.wrap
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.io.ByteArrayInputStream
import java.util.NoSuchElementException
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.sql.rowset.serial.SerialBlob

fun Column<DateTime>.lastWeek() = GreaterEqOp(this, Delay("1 week"))
fun Column<DateTime>.lastMonth() = GreaterEqOp(this, Delay("1 month"))

class Delay(val delay: String = "1 week") : Function<DateTime>(DateColumnType(true)) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append("CURRENT_TIMESTAMP - interval '$delay'")
    }
}

class FullTextSearchOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "@@")

infix fun ExpressionWithColumnType<Any>.fullTextSearch(t: String): Op<Boolean> {
    return FullTextSearchOp(this, ToTsQuery(wrap(t)))
}

infix fun ExpressionWithColumnType<Any>.fullTextSearchEnglish(t: String): Op<Boolean> {
    return FullTextSearchOp(this, ToTsQuery(wrap(t), "pg_catalog.english"))
}

class ToTsQuery<T : String?>(val expr: Expression<T>, val dictionary: String = "pg_catalog.simple") : Function<T>(VarCharColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append("to_tsquery('$dictionary', ")
        expr.toQueryBuilder(queryBuilder)
        append(")")
    }
}

class SqlData(
    dry: Boolean = false,
    type: String = "postgresql",
    host: String = "localhost",
    port: Int = 5432,
    name: String = "centyllion",
    user: String = "centyllion",
    password: String = ""
) : Data {

    val url = "jdbc:$type://$host:$port/$name"

    val dataSource = HikariDataSource().apply {
        jdbcUrl = url
        username = user
        setPassword(password)
    }

    val database = Database.connect(dataSource)

    init {
        if (!dry) {
            transaction(database) {
                SchemaUtils.createMissingTablesAndColumns(
                    DbMetaTable,
                    DbUsers,
                    DbFeaturedTable,
                    DbAssets,
                    DbDescriptionInfos,
                    DbModelDescriptions,
                    DbSimulationDescriptions
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
    }

    override fun usersInfo(): CollectionInfo = transaction(database) {
        // TODO count user last seen
        CollectionInfo(
            DbUser.count(),
            0,
            0
        )
    }

    override fun getAllUsers(detailed: Boolean, offset: Int, limit: Int): ResultPage<User> = transaction(database) {
        val content = DbUser.all().map { it -> it.toModel(detailed) }
        ResultPage(content, offset, DbUser.all().count())
    }

    override fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User {
        val currentName = principal.payload.claims["name"]?.asString() ?: ""
        val currentUsername = principal.payload.claims["preferred_username"]?.asString() ?: ""
        val currentEmail = principal.payload.claims["email"]?.asString() ?: ""

        val existing = transaction(database) {
            DbUser.find { DbUsers.keycloak eq principal.payload.subject }.firstOrNull()
        }

        val user = existing ?: transaction(database) {
            DbUser.new {
                keycloak = principal.payload.subject
                name = currentName
                username = currentUsername
                email = currentEmail
            }
        }

        if (user.name != currentName || user.username != currentUsername) {
            transaction {
                // updates roles for user
                user.name = currentName
                user.username = currentUsername
            }
        }

        return user.toModel(true)
    }

    override fun getUser(id: String, detailed: Boolean): User? = transaction(database) {
        val user = DbUser.findById(UUID.fromString(id))
        user?.toModel(detailed)
    }

    override fun getUserFromKeycloakId(keycloakId: String, detailed: Boolean): User? = transaction(database) {
        DbUser.find { DbUsers.keycloak eq keycloakId }.firstOrNull()?.toModel(detailed)
    }

    override fun saveUser(user: User) {
        transaction(database) { DbUser.findById(UUID.fromString(user.id))?.fromModel(user) }
    }

    override fun grainModelsInfo(): CollectionInfo = transaction(database) {
        CollectionInfo(
            DbModelDescription.count(),
            DbModelDescriptions.innerJoin(DbDescriptionInfos).select(DbDescriptionInfos.lastModifiedOn.lastWeek()).count(),
            DbModelDescriptions.innerJoin(DbDescriptionInfos).select(DbDescriptionInfos.lastModifiedOn.lastMonth()).count()
        )
    }

    private fun grainModelsQuery(callerUUID: UUID?, userUUID: UUID?) = DbModelDescriptions
        .innerJoin(DbDescriptionInfos)
        .select {
            listOfNotNull( userUUID?.let { DbDescriptionInfos.userId eq it },
                (DbDescriptionInfos.readAccess eq true) or if (callerUUID != null) DbDescriptionInfos.userId eq callerUUID else Op.FALSE
            ).fold(Op.TRUE as Op<Boolean>) {a, c -> a and c }
        }
        .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)

    override fun grainModels(callerId: String?, userId: String?, offset: Int, limit: Int): ResultPage<GrainModelDescription> =
        transaction(database) {
            val callerUUID = callerId?.let { UUID.fromString(it) }
            val userUUID = userId?.let { UUID.fromString(it) }
            val content = DbModelDescription.wrapRows(
                grainModelsQuery(callerUUID, userUUID)
                    .limit(limit, offset)
                    .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
            ).map { it.toModel() }
            ResultPage(content, offset, grainModelsQuery(callerUUID, userUUID).count())
        }

    override fun getGrainModel(id: String) = transaction(database) {
        DbModelDescription.findById(UUID.fromString(id))?.toModel()
    }

    override fun createGrainModel(userId: String, sent: GrainModel): GrainModelDescription = transaction(database) {
        val newInfo = DbDescriptionInfo.new {
            this.userId = UUID.fromString(userId)
            createdOn = DateTime.now()
            lastModifiedOn = DateTime.now()
            readAccess = true
        }
        DbModelDescription.new {
            info = newInfo
            model = Json.stringify(GrainModel.serializer(), sent)
            version = 0
            type = DbModelType.Grain.toString()
        }.toModel()
    }

    override fun saveGrainModel(model: GrainModelDescription) {
        transaction(database) {
            val found = DbModelDescription.findById(UUID.fromString(model.id))
            found?.fromModel(model)
            found?.info?.lastModifiedOn = DateTime.now()
        }
    }

    override fun deleteGrainModel(modelId: String) {
        transaction(database) {
            DbSimulationDescription
                .find { DbSimulationDescriptions.modelId eq UUID.fromString(modelId) }
                .forEach { deleteSimulation(it) }
            DbModelDescription.findById(UUID.fromString(modelId))?.let {
                it.delete()
                it.info.delete()
            }
        }
    }

    override fun simulationsInfo(): CollectionInfo = transaction(database) {
        CollectionInfo(
            DbSimulationDescription.count(),
            DbSimulationDescriptions.innerJoin(DbDescriptionInfos).select(DbDescriptionInfos.lastModifiedOn.lastWeek()).count(),
            DbSimulationDescriptions.innerJoin(DbDescriptionInfos).select(DbDescriptionInfos.lastModifiedOn.lastMonth()).count()
        )
    }

    private fun simulationsQuery(callerUUID: UUID?, userUUID: UUID?, modelUUID: UUID?) = DbSimulationDescriptions
        .innerJoin(DbDescriptionInfos)
        .select {
            listOfNotNull(
                userUUID?.let { DbDescriptionInfos.userId eq it },
                modelUUID?.let { DbSimulationDescriptions.modelId eq it },
                (DbDescriptionInfos.readAccess eq true) or if (callerUUID != null) DbDescriptionInfos.userId eq callerUUID else Op.FALSE
            ).fold(Op.TRUE as Op<Boolean>) {a, c -> a and c }
        }
        .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)

    override fun simulations(callerId: String?, userId: String?, modelId: String?, offset: Int, limit: Int): ResultPage<SimulationDescription> =
        transaction(database) {
            val callerUUID = callerId?.let { UUID.fromString(it) }
            val userUUID = userId?.let { UUID.fromString(it) }
            val modelUUID = modelId?.let { UUID.fromString(it) }
            val content = DbSimulationDescription.wrapRows(
                simulationsQuery(callerUUID, userUUID, modelUUID)
                    .limit(limit, offset)
                    .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
            ).map { it.toModel() }
            ResultPage(content, offset, simulationsQuery(callerUUID, userUUID, modelUUID).count())
        }

    override fun getSimulation(id: String) = transaction(database) {
        DbSimulationDescription.findById(UUID.fromString(id))?.toModel()
    }

    override fun createSimulation(userId: String, modelId: String, sent: Simulation) =
        transaction(database) {
            val newInfo = DbDescriptionInfo.new {
                this.userId = UUID.fromString(userId)
                createdOn = DateTime.now()
                lastModifiedOn = DateTime.now()
                readAccess = true
            }

            DbSimulationDescription.new {
                info = newInfo
                this.modelId = UUID.fromString(modelId)
                simulation = Json.stringify(Simulation.serializer(), sent)
                version = 0
                type = DbModelType.Grain.toString()
            }.toModel()
        }

    override fun saveSimulation(simulation: SimulationDescription) {
        transaction(database) {
            val found = DbSimulationDescription.findById(UUID.fromString(simulation.id))
            found?.fromModel(simulation)
            found?.info?.lastModifiedOn = DateTime.now()
        }
    }

    override fun deleteSimulation(simulationId: String) {
        transaction(database) { deleteSimulation(DbSimulationDescription.findById(UUID.fromString(simulationId))) }
    }

    /** Must be called inside transaction */
    private fun deleteSimulation(simulation: DbSimulationDescription?) {
        simulation?.let {
            it.delete()
            it.info.delete()
            it.thumbnailId?.let { deleteAsset(it.toString()) }
        }
    }

    override fun getAllFeatured(offset: Int, limit: Int) = transaction(database) {
        val content = DbFeatured.all().limit(limit, offset).reversed().map { it.toModel() }
        ResultPage(content, offset, DbFeatured.all().count())
    }

    override fun getFeatured(id: String) = transaction(database) {
        DbFeatured.findById(UUID.fromString(id))?.toModel()
    }

    override fun createFeatured(simulationId: String) = transaction(database) {
        DbFeatured.new { featuredId = UUID.fromString(simulationId) }.toModel()
    }

    override fun deleteFeatured(featuredId: String) {
        transaction(database) {
            DbFeatured.findById(UUID.fromString(featuredId))?.delete()
        }
    }

    private fun searchSimulationQuery(query: String) = DbSimulationDescriptions
        .innerJoin(DbDescriptionInfos)
        .select {
            val q = if (query.isNotBlank()) "$query:*" else ""
            (DbDescriptionInfos.readAccess eq true) and (DbSimulationDescriptions.searchable fullTextSearchEnglish q)
        }


    override fun searchSimulation(query: String, offset: Int, limit: Int) = transaction {
        val content = DbSimulationDescription.wrapRows(
            searchSimulationQuery(query)
                .limit(limit, offset)
                .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }

        ResultPage(content, offset, searchSimulationQuery(query).count())
    }

    override fun modelTags(userId: String?, offset: Int, limit: Int): ResultPage<String> = transaction {
        val request = "SELECT tags_searchable FROM modeldescriptions " +
                "INNER JOIN infodescriptions ON modeldescriptions.info = infodescriptions.id " +
                "WHERE " + if (userId != null) "infodescriptions.\"userId\" = ''$userId''" else  "infodescriptions.\"readAccess\""

        exec("SELECT word FROM ts_stat('$request') ORDER BY ndoc DESC LIMIT $limit OFFSET $offset") {
            val result = mutableListOf<String>()
            while (it.next()) { result.add(it.getString(1)) }
            ResultPage(result, offset, result.size)
        } ?: ResultPage(emptyList(), offset, 0)
    }

    private fun searchModelQuery(query: String, tags: List<String>) = DbModelDescriptions.innerJoin(DbDescriptionInfos).select {
        val op = if (query.isNotBlank()) DbModelDescriptions.searchable fullTextSearchEnglish "$query:*" else null
        (tags.map { DbModelDescriptions.tags_searchable fullTextSearch it } + op)
            .filterNotNull()
            .fold(DbDescriptionInfos.readAccess eq true) {a, c -> a and c }
    }

    override fun searchModel(query: String, tags: List<String>, offset: Int, limit: Int) = transaction {
        val content = DbModelDescription.wrapRows(
            searchModelQuery(query, tags)
                .limit(limit, offset)
                .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }

        ResultPage(content, offset, searchModelQuery(query, tags).count())
    }

    private fun assetExtension(extension: String) = DbAssets.name like "%.$extension"

    private fun assetsRequest(extensions: List<String>) =
        if (extensions.isEmpty()) DbAsset.all() else DbAsset.find {
            val first = assetExtension(extensions.first())
            extensions.drop(1).fold(first) { a, c -> a or assetExtension(c) }
        }

    override fun getAllAssets(offset: Int, limit: Int, extensions: List<String>) = transaction(database) {
        val content = assetsRequest(extensions).limit(limit, offset).reversed().map { it.toModel() }
        ResultPage(content, offset, assetsRequest(extensions).count())
    }

    override fun assetsForUser(userId: String) =
        DbAsset.find { DbAssets.userId eq UUID.fromString(userId) }.map { it.toModel() }

    override fun getAsset(id: String) = transaction(database) {
        DbAsset.findById(UUID.fromString(id))?.toModel()
    }

    override fun getAssetContent(id: String) = transaction(database) {
        DbAsset.findById(UUID.fromString(id))?.content?.let { it.getBytes(1, it.length().toInt()) }
    }

    override fun createAsset(name: String, userId: String, data: ByteArray): Asset = transaction(database) {
        DbAsset.new {
            this.name = name
            this.entries = if (name.endsWith(".zip")) listZipEntries(data).joinToString(",") else ""
            this.userId = UUID.fromString(userId)
            this.content = SerialBlob(data)
        }.toModel()
    }

    override fun deleteAsset(id: String) = deleteAsset(UUID.fromString(id))

    fun deleteAsset(id: UUID) {
        transaction(database) { DbAsset.findById(id)?.delete() }
    }
}

fun listZipEntries(data: ByteArray): MutableList<String> {
    return ZipInputStream(ByteArrayInputStream(data)).use {
        val entries = mutableListOf<String>()
        var entry = it.nextEntry
        while (entry != null) {
            entries.add(entry.name)
            entry = it.nextEntry
        }
        entries
    }
}

package com.centyllion.backend.data

import com.centyllion.common.SubscriptionType
import com.centyllion.common.topGroup
import com.centyllion.model.Asset
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ResultPage
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.Subscription
import com.centyllion.model.SubscriptionParameters
import com.centyllion.model.SubscriptionState
import com.centyllion.model.User
import com.zaxxer.hikari.HikariDataSource
import io.ktor.auth.jwt.JWTPrincipal
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Function
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
import org.joda.time.DateTimeUtils
import java.util.NoSuchElementException
import java.util.UUID
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
                DbMetaTable,
                DbUsers,
                DbFeaturedTable,
                DbAssets,
                DbDescriptionInfos,
                DbModelDescriptions,
                DbSimulationDescriptions,
                DbSubscriptions
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

    override fun getAllUsers(detailed: Boolean, offset: Int, limit: Int): ResultPage<User> = transaction(database) {
        val content = DbUser.all().limit(limit, offset).reversed().map { it.toModel(detailed) }
        ResultPage(content, offset, DbUser.all().count())
    }

    override fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User {
        // retrieves roles from claim
        val currentGroup = principal.payload.claims["groups"]?.asList(String::class.java)
            ?.map { SubscriptionType.valueOf(it) }?.topGroup() ?: SubscriptionType.Apprentice

        val currentName = principal.payload.claims["name"]?.asString() ?: ""
        val currentUsername = principal.payload.claims["preferred_username"]?.asString() ?: ""
        val currentEmail = principal.payload.claims["email"]?.asString() ?: ""

        // find or create the user
        val user = transaction(database) {
            DbUser.find { DbUsers.keycloak eq principal.payload.subject }.firstOrNull()
        } ?: transaction(database) {
            DbUser.new {
                keycloak = principal.payload.subject
                name = currentName
                username = currentUsername
                email = currentEmail
                subscription = currentGroup.name
            }
        }

        /* This shouldn't be needed anymore */
        if (user.name != currentName || user.subscription != currentGroup.name || user.username != currentUsername) {
            // updates roles for user
            transaction {
                user.name = currentName
                user.username = currentUsername
                user.subscription = currentGroup.name
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

    private fun publicGrainModelQuery() = DbModelDescriptions.innerJoin(DbDescriptionInfos).select {
        (DbDescriptionInfos.id eq DbModelDescriptions.info) and (DbDescriptionInfos.readAccess eq true)
    }

    override fun publicGrainModels(offset: Int, limit: Int) = transaction(database) {
        val content = DbModelDescription.wrapRows(
            publicGrainModelQuery().limit(limit, offset).orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }

        ResultPage(content, offset, publicGrainModelQuery().count())
    }

    private fun grainModelsForUserQuery(userUUID: UUID) = DbModelDescriptions
        .innerJoin(DbDescriptionInfos)
        .select { DbDescriptionInfos.userId eq userUUID }
        .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)

    override fun grainModelsForUser(userId: String, offset: Int, limit: Int): ResultPage<GrainModelDescription> =
        transaction(database) {
            val userUUID = UUID.fromString(userId)
            val content = DbModelDescription.wrapRows(
                grainModelsForUserQuery(userUUID).limit(limit, offset).orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
            ).map { it.toModel() }
            ResultPage(content, offset, grainModelsForUserQuery(userUUID).count())
        }

    override fun getGrainModel(id: String) = transaction(database) {
        DbModelDescription.findById(UUID.fromString(id))?.toModel()
    }

    override fun createGrainModel(userId: String, sent: GrainModel): GrainModelDescription = transaction(database) {
        val newInfo = DbDescriptionInfo.new {
            this.userId = UUID.fromString(userId)
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

    private fun publicSimulationQuery(modelId: UUID?) =
        DbSimulationDescriptions.innerJoin(DbDescriptionInfos).select {
            val public = (DbDescriptionInfos.id eq DbSimulationDescriptions.info) and (DbDescriptionInfos.readAccess eq true)
            when (modelId) {
                null -> public
                else -> public and (DbSimulationDescriptions.modelId eq modelId)
            }
        }

    override fun publicSimulations(modelId: String?, offset: Int, limit: Int) = transaction(database) {
        val modelUUID = modelId?.let { UUID.fromString(it) }
        val content = DbSimulationDescription.wrapRows(
            publicSimulationQuery(modelUUID).limit(limit, offset).orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }

        ResultPage(content, offset, publicSimulationQuery(modelUUID).count())
    }

    private fun simulationsForUserQuery(userUUID: UUID, modelId: UUID?) = DbSimulationDescriptions
        .innerJoin(DbDescriptionInfos)
        .select {
            val user = DbDescriptionInfos.userId eq userUUID
            when (modelId) {
                null -> user
                else -> user and (DbSimulationDescriptions.modelId eq modelId)
            }
        }
        .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)

    override fun simulationsForUser(userId: String, modelId: String?, offset: Int, limit: Int): ResultPage<SimulationDescription> =
        transaction(database) {
            val userUUID = UUID.fromString(userId)
            val modelUUID = modelId?.let { UUID.fromString(it) }
            val content = DbSimulationDescription.wrapRows(
                simulationsForUserQuery(userUUID, modelUUID)
                    .limit(limit, offset)
                    .orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
            ).map { it.toModel() }
            ResultPage(content, offset, simulationsForUserQuery(userUUID, modelUUID).count())
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
            (DbDescriptionInfos.readAccess eq true) and (DbSimulationDescriptions.searchable fullTextSearch q)
        }


    override fun searchSimulation(query: String, offset: Int, limit: Int) = transaction {
        val content = DbSimulationDescription.wrapRows(
            searchSimulationQuery(query).orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }

        ResultPage(content, offset, searchSimulationQuery(query).count())
    }

    private fun searchModelQuery(query: String) = DbModelDescriptions.innerJoin(DbDescriptionInfos).select {
        val q = if (query.isNotBlank()) "$query:*" else ""
        (DbDescriptionInfos.readAccess eq true) and (DbModelDescriptions.searchable fullTextSearch q)
    }

    override fun searchModel(query: String, offset: Int, limit: Int) = transaction {
        val content = DbModelDescription.wrapRows(
            searchModelQuery(query).orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }

        ResultPage(content, offset, searchModelQuery(query).count())
    }

    override fun subscriptionsForUser(userId: String) =
        subscriptionsForUser(UUID.fromString(userId))

    private fun subscriptionsForUser(userId: UUID) = transaction(database) {
        val now = DateTimeUtils.currentTimeMillis()
        DbSubscription
            .find { DbSubscriptions.userId eq userId }
            .map { it.toModel() }
    }

    override fun getSubscription(id: String): Subscription? = transaction(database) {
        DbSubscription.findById(UUID.fromString(id))?.toModel()
    }

    override fun createSubscription(
        userId: String,
        sandbox: Boolean,
        parameters: SubscriptionParameters
    ): Subscription = transaction(database) {
        DbSubscription.new {
            this.userId = UUID.fromString(userId)
            this.sandbox = sandbox
            autoRenew = parameters.autoRenew
            startedOn = DateTime.now()
            expiresOn = parameters.duration.let { if (it > 0) startedOn.plus(it) else null }
            subscription = parameters.subscription.name
            duration = parameters.duration
            amount = parameters.amount
            paymentMethod = parameters.paymentMethod
            state = SubscriptionState.Waiting.name
        }.toModel()
    }

    override fun saveSubscription(subscription: Subscription) {
        transaction(database) {
            val found = DbSubscription.findById(UUID.fromString(subscription.id))
            found?.fromModel(subscription)
        }
    }

    override fun deleteSubscription(subscriptionId: String) {
        transaction(database) {
            DbSubscription.findById(UUID.fromString(subscriptionId))?.delete()
        }
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

    override fun getAssetContent(id: String) = transaction(database) {
        DbAsset.findById(UUID.fromString(id))?.content?.let { it.getBytes(1, it.length().toInt()) }
    }

    override fun createAsset(name: String, userId: String, data: ByteArray): Asset = transaction(database) {
        DbAsset.new {
            this.name = name
            this.userId = UUID.fromString(userId)
            this.content = SerialBlob(data)
        }.toModel()
    }

    override fun deleteAsset(id: String) = deleteAsset(UUID.fromString(id))

    fun deleteAsset(id: UUID) {
        transaction(database) { DbAsset.findById(id)?.delete() }
    }
}


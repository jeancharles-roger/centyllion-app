package com.centyllion.backend.data

import com.centyllion.backend.AuthorizationManager
import com.centyllion.backend.createThumbnail
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.wrap
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.and
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
    val authorizationManager: AuthorizationManager,
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
            ?.map { SubscriptionType.valueOf(it) }?.topGroup() ?: SubscriptionType.Free

        val currentUsername = principal.payload.claims["preferred_username"]?.asString() ?: ""

        // find or create the user
        val user = transaction(database) {
            DbUser.find { DbUsers.keycloak eq principal.payload.subject }.firstOrNull()
        } ?: principal.payload.claims.let { claims ->
            transaction(database) {
                DbUser.new {
                    keycloak = principal.payload.subject
                    name = claims["name"]?.asString() ?: ""
                    username = currentUsername
                    email = claims["email"]?.asString() ?: ""
                    subscription = currentGroup.name
                }
            }
        }
        /* This shouldn't be needed anymore
        if (user.subscription != currentGroup.name || user.username != currentUsername) {
            // updates roles for user
            transaction {
                user.username = currentUsername
                user.subscription = currentGroup.name
            }
        }
         */

        transaction { updateUserSubscription(user) }

        return user.toModel(true)
    }

    override fun getUser(id: String, detailed: Boolean): User? = transaction(database) {
        val user = DbUser.findById(UUID.fromString(id))
        if (user != null) updateUserSubscription(user)
        user?.toModel(detailed)
    }

    /** Must be called inside a transition */
    private fun updateUserSubscription(user: DbUser) {
        val date = user.subscriptionUpdatedOn

        // updates at most one time a day
        if (date == null || date.plusDays(1).isBeforeNow) {
            val subscriptions = subscriptionsForUser(user.id.value, false)
            val type = subscriptions.map { it.subscription }.topGroup()
            user.subscription = type.name
            user.subscriptionUpdatedOn = DateTime.now()
        }
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

    override fun grainModelsForUser(userId: String): List<GrainModelDescription> = transaction(database) {
        val userUUID = UUID.fromString(userId)
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

    private fun publicSimulationQuery() = DbSimulationDescriptions.innerJoin(DbDescriptionInfos).select {
        (DbDescriptionInfos.id eq DbSimulationDescriptions.info) and (DbDescriptionInfos.readAccess eq true)
    }

    override fun publicSimulations(offset: Int, limit: Int) = transaction(database) {
        val content = DbSimulationDescription.wrapRows(
            publicSimulationQuery().limit(limit, offset).orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }

        ResultPage(content, offset, publicSimulationQuery().count())
    }

    override fun getSimulationForModel(modelId: String): List<SimulationDescription> = transaction(database) {
        DbSimulationDescription
            .find { DbSimulationDescriptions.modelId eq UUID.fromString(modelId) }
            .map { it.toModel() }
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
        // TODO thumbnails should be sent from client
        transaction(database) {
            val found = DbSimulationDescription.findById(UUID.fromString(simulation.id))
            // removes current asset if exists
            val toSave = simulation.let {
                simulation.thumbnailId?.let { deleteAsset(it) }
                // creates new asset
                val asset = getGrainModel(simulation.modelId)?.let {
                    createAsset(
                        "${simulation.simulation.name}.png",
                        createThumbnail(it.model, simulation.simulation)
                    )
                }
                simulation.copy(thumbnailId = asset?.id)
            }
            // updates simulation
            found?.fromModel(toSave)
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
            (DbDescriptionInfos.readAccess eq true) and (DbSimulationDescriptions.searchable fullTextSearch query)
        }


    override fun searchSimulation(query: String, offset: Int, limit: Int) = transaction {
        val content = DbSimulationDescription.wrapRows(
            searchSimulationQuery(query).orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }

        ResultPage(content, offset, searchSimulationQuery(query).count())
    }

    private fun searchModelQuery(query: String) = DbModelDescriptions.innerJoin(DbDescriptionInfos).select {
        (DbDescriptionInfos.readAccess eq true) and (DbModelDescriptions.searchable fullTextSearch query)
    }

    override fun searchModel(query: String, offset: Int, limit: Int) = transaction {
        val content = DbModelDescription.wrapRows(
            searchModelQuery(query).orderBy(DbDescriptionInfos.lastModifiedOn, SortOrder.DESC)
        ).map { it.toModel() }

        ResultPage(content, offset, searchModelQuery(query).count())
    }

    override fun subscriptionsForUser(userId: String, all: Boolean) =
        subscriptionsForUser(UUID.fromString(userId), all)

    private fun subscriptionsForUser(userId: UUID, all: Boolean) = transaction(database) {
        val now = DateTimeUtils.currentTimeMillis()
        DbSubscription
            .find { DbSubscriptions.userId eq userId }
            .map { it.toModel() }
            // TODO makes this filter in the where close of the SQL query
            .filter { all || it.active(now) }
    }

    override fun getSubscription(id: String): Subscription? = transaction(database) {
        DbSubscription.findById(UUID.fromString(id))?.toModel()
    }

    override fun createSubscription(userId: String, sandbox: Boolean, parameters: SubscriptionParameters): Subscription = transaction(database) {
        DbSubscription.new {
            this.userId = UUID.fromString(userId)
            this.sandbox = sandbox
            autoRenew = parameters.autoRenew
            startedOn = DateTime.now()
            expiresOn = parameters.duration.let { if (it > 0) startedOn.plusDays(it + 1) else null }
            subscription = parameters.subscription.name
            duration = parameters.duration
            amount = parameters.amount
            paymentMethod = parameters.paymentMethod
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


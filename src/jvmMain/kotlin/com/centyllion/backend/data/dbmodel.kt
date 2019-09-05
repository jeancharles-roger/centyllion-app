package com.centyllion.backend.data

import com.centyllion.common.SubscriptionType
import com.centyllion.model.Asset
import com.centyllion.model.DescriptionInfo
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.Subscription
import com.centyllion.model.SubscriptionState
import com.centyllion.model.User
import com.centyllion.model.UserDetails
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.ColumnType
import org.joda.time.DateTime
import java.util.UUID

class TsVectorColumnType : ColumnType()  {
    override fun sqlType() = "tsvector"
}

object DbMetaTable : UUIDTable("meta") {
    val version = integer("version")
}

class DbMeta(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbMeta>(DbMetaTable)

    var version by DbMetaTable.version
}

object DbUsers : UUIDTable("users") {
    val name = text("name")
    val username = text("username").default("")
    val keycloak = text("keycloak")
    val subscriptionUpdatedOn = datetime("subscriptionUpdatedOn").nullable()

    // Details
    val email = text("email")
    val subscription = text("subscription").default("Free")
    val stripe = text("stripe").nullable()
}

class DbUser(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbUser>(DbUsers)

    var keycloak by DbUsers.keycloak
    var name by DbUsers.name
    var username by DbUsers.username
    var subscriptionUpdatedOn by DbUsers.subscriptionUpdatedOn
    var email by DbUsers.email
    var subscription by DbUsers.subscription
    var stripe by DbUsers.stripe

    fun toModel(detailed: Boolean): User {
        val subscriptionType = SubscriptionType.parse(subscription)
        val details = UserDetails(keycloak, email, stripe, subscriptionType, subscriptionUpdatedOn?.millis)
        return User(id.toString(), name, username, if (detailed) details else null)
    }

    fun fromModel(source: User) {
        name = source.name
        username = source.username
        source.details?.let {
            email = it.email
            stripe = it.stripeId
            if (subscription != it.subscription.name) {
                subscription = it.subscription.name
                subscriptionUpdatedOn = DateTime.now()
            }
        }
    }
}

object DbDescriptionInfos : UUIDTable("infoDescriptions") {
    val userId = uuid("userId").nullable()
    val createdOn = datetime("createdOn")
    val lastModifiedOn = datetime("lastModifiedOn")
    val readAccess = bool("readAccess")
    val cloneAccess = bool("cloneAccess")
}

class DbDescriptionInfo(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbDescriptionInfo>(DbDescriptionInfos)

    var userId by DbDescriptionInfos.userId
    var createdOn by DbDescriptionInfos.createdOn
    var lastModifiedOn by DbDescriptionInfos.lastModifiedOn
    var readAccess by DbDescriptionInfos.readAccess
    var cloneAccess by DbDescriptionInfos.cloneAccess

    fun toModel(): DescriptionInfo = DescriptionInfo(
        userId?.let { DbUser.findById(it) }?.toModel(false),
        createdOn.toString(), lastModifiedOn.toString(),
        readAccess, cloneAccess
    )

    fun fromModel(source: DescriptionInfo) {
        userId = source?.user?.id?.let { UUID.fromString(it) }
        createdOn = DateTime.parse(source.createdOn)
        lastModifiedOn = DateTime.parse(source.lastModifiedOn)
        readAccess = source.readAccess
        cloneAccess = source.cloneAccess
    }
}

enum class DbModelType { Grain }

object DbModelDescriptions : UUIDTable("modelDescriptions") {
    val info = reference("info", DbDescriptionInfos)
    val tags = text("tags").default("")
    val tags_searchable = registerColumn<Any>("tags_searchable", TsVectorColumnType())
    val model = text("model")
    val type = text("type")
    val version = integer("version")
    val searchable = registerColumn<Any>("searchable", TsVectorColumnType())
}

class DbModelDescription(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbModelDescription>(DbModelDescriptions)

    var info by DbDescriptionInfo referencedOn DbModelDescriptions.info
    var tags by DbModelDescriptions.tags
    var model by DbModelDescriptions.model
    var type by DbModelDescriptions.type
    var version by DbModelDescriptions.version

    fun toModel(): GrainModelDescription {
        // TODO handle migrations
        val model = Json.parse(GrainModel.serializer(), model)
        return GrainModelDescription(id.toString(), info.toModel(), tags, model)
    }

    fun fromModel(source: GrainModelDescription) {
        info.fromModel(source.info)
        tags = source.tags
        // TODO handle migrations
        model = Json.stringify(GrainModel.serializer(), source.model)
    }
}

object DbSimulationDescriptions : UUIDTable("simulationDescriptions") {
    val info = reference("info", DbDescriptionInfos)
    val modelId = uuid("modelId")
    val thumbnailId = uuid("thumbnailId").nullable()
    val simulation = text("simulation")
    val type = text("type")
    val version = integer("version")
    val searchable = registerColumn<Any>("searchable", TsVectorColumnType())
}

class DbSimulationDescription(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbSimulationDescription>(DbSimulationDescriptions)

    var info by DbDescriptionInfo referencedOn DbSimulationDescriptions.info
    var modelId by DbSimulationDescriptions.modelId
    var thumbnailId by DbSimulationDescriptions.thumbnailId
    var simulation by DbSimulationDescriptions.simulation
    var type by DbSimulationDescriptions.type
    var version by DbSimulationDescriptions.version

    fun toModel(): SimulationDescription {
        // TODO handle migrations
        val simulation = Json.parse(Simulation.serializer(), simulation)
        return SimulationDescription(id.toString(), info.toModel(), modelId.toString(), thumbnailId?.toString(), simulation)
    }

    fun fromModel(source: SimulationDescription) {
        // TODO handle migrations
        info.fromModel(source.info)
        simulation = Json.stringify(Simulation.serializer(), source.simulation)
        modelId = UUID.fromString(source.modelId)
        thumbnailId = if (source.thumbnailId != null) UUID.fromString(source.thumbnailId) else null
    }

}

object DbFeaturedTable : UUIDTable("featured") {
    val featuredId = uuid("featuredId")
}

class DbFeatured(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbFeatured>(DbFeaturedTable)

    var featuredId by DbFeaturedTable.featuredId

    fun toModel(): FeaturedDescription {
        // TODO find a clearer way
       return  DbSimulationDescription.findById(featuredId)?.let { simulation ->
            DbModelDescription.findById(simulation.modelId)?.let { model ->
                val simulationModel = simulation.toModel()
                val modelModel = model.toModel()
                FeaturedDescription(
                    id.toString(), simulationModel.info.lastModifiedOn, simulationModel.thumbnailId,
                    modelModel.id, simulationModel.id, simulationModel.info.user?.id ?: "",
                    listOf(simulationModel.simulation.name, modelModel.model.name).filter { it.isNotEmpty() }.joinToString(" / "),
                    listOf(simulationModel.simulation.description, modelModel.model.description).filter { it.isNotEmpty() }.joinToString("\n"),
                    ""
                )
            }
        }?: FeaturedDescription(id.toString(), "", null, "", featuredId.toString(), "", "", "", "")
    }

    fun fromModel(source: FeaturedDescription) {
       featuredId = UUID.fromString(source.simulationId)
    }
}

object DbSubscriptions : UUIDTable("subscriptions") {
    val userId = uuid("userId")

    val sandbox = bool("sandbox")
    val autoRenew = bool("autoRenew").default(true)
    val cancelled = bool("cancelled").default(false)

    val startedOn = datetime("startedOn")
    val payedOn = datetime("payedOn").nullable()
    val expiresOn = datetime("expiresOn").nullable()
    val cancelledOn = datetime("cancelledOn").nullable()

    val subscription = text("subscription")
    val duration = long("duration")
    val amount = double("amount")
    val paymentMethod = text("paymentMethod")

    val state = text("state")
}

class DbSubscription(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbSubscription>(DbSubscriptions)

    var userId by DbSubscriptions.userId

    var sandbox by DbSubscriptions.sandbox
    var autoRenew by DbSubscriptions.autoRenew
    var cancelled by DbSubscriptions.cancelled

    var startedOn by DbSubscriptions.startedOn
    var payedOn by DbSubscriptions.payedOn
    var expiresOn by DbSubscriptions.expiresOn
    var cancelledOn by DbSubscriptions.cancelledOn

    var subscription by DbSubscriptions.subscription
    var duration by DbSubscriptions.duration
    var amount by DbSubscriptions.amount
    var paymentMethod by DbSubscriptions.paymentMethod

    var state by DbSubscriptions.state

    fun toModel() = Subscription(
        id.toString(), userId.toString(), sandbox, autoRenew, cancelled,
        startedOn.millis, payedOn?.millis, expiresOn?.millis, cancelledOn?.millis,
        SubscriptionType.parse(subscription), duration, amount, paymentMethod,
        SubscriptionState.valueOf(state)
    )

    fun fromModel(source: Subscription) {
        userId = UUID.fromString(source.userId)
        sandbox = source.sandbox
        autoRenew = source.autoRenew
        cancelled = source.cancelled
        startedOn = DateTime(source.startedOn)
        payedOn = source.payedOn?.let { DateTime(it) }
        expiresOn = source.expiresOn?.let { DateTime(it) }
        cancelledOn = source.cancelledOn?.let { DateTime(it) }

        subscription = source.subscription.name
        duration = source.duration
        amount = source.amount
        paymentMethod = source.paymentMethod
        state = source.state.name
    }

}

object DbAssets : UUIDTable("assets") {
    val name = text("name")
    val userId = uuid("userId").default(UUID.fromString("00000000-0000-0000-0000-000000000000"))
    val content = blob("content")
}

class DbAsset(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbAsset>(DbAssets)

    var name by DbAssets.name
    var userId by DbAssets.userId
    var content by DbAssets.content

    fun toModel() = Asset(
        id.toString(), name, userId.toString()
    )

    fun fromModel(source: Asset) {
        name = source.name
        userId = UUID.fromString(source.userId)
    }
}

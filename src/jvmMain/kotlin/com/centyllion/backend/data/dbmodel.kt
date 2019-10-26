@file:UseExperimental(UnstableDefault::class)
package com.centyllion.backend.data

import com.centyllion.model.Asset
import com.centyllion.model.DescriptionInfo
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.User
import com.centyllion.model.UserDetails
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.DateColumnType
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.QueryBuilder
import org.joda.time.DateTime
import java.util.UUID

class TsVectorColumnType : ColumnType()  {
    override fun sqlType() = "tsvector"
}

class MinInfinity : Function<DateTime>(DateColumnType(false)) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder { append("'-infinity'") }
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

    // Details
    val email = text("email")
    val tutorialDone = bool("tutorialDone").default(false)
}

class DbUser(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbUser>(DbUsers)

    var keycloak by DbUsers.keycloak
    var name by DbUsers.name
    var username by DbUsers.username
    var email by DbUsers.email
    var tutorialDone by DbUsers.tutorialDone

    fun toModel(detailed: Boolean): User {
        val details = UserDetails(keycloak, email, tutorialDone)
        return User(id.toString(), name, username, if (detailed) details else null)
    }

    fun fromModel(source: User) {
        name = source.name
        username = source.username
        source.details?.let {
            email = it.email
            tutorialDone = it.tutorialDone
        }
    }
}

object DbDescriptionInfos : UUIDTable("infoDescriptions") {
    val userId = uuid("userId").nullable()
    val createdOn = datetime("createdOn")
    val lastModifiedOn = datetime("lastModifiedOn")
    val readAccess = bool("readAccess").default(true)
}

class DbDescriptionInfo(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbDescriptionInfo>(DbDescriptionInfos)

    var userId by DbDescriptionInfos.userId
    var createdOn by DbDescriptionInfos.createdOn
    var lastModifiedOn by DbDescriptionInfos.lastModifiedOn
    var readAccess by DbDescriptionInfos.readAccess

    fun toModel(): DescriptionInfo = DescriptionInfo(
        userId?.let { DbUser.findById(it) }?.toModel(false),
        createdOn.toString(), lastModifiedOn.toString(),
        readAccess
    )

    fun fromModel(source: DescriptionInfo) {
        userId = source.user?.id?.let { UUID.fromString(it) }
        createdOn = DateTime.parse(source.createdOn)
        lastModifiedOn = DateTime.parse(source.lastModifiedOn)
        readAccess = source.readAccess
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

object DbAssets : UUIDTable("assets") {
    val name = text("name")
    val entries = text("entries").default("")
    val userId = uuid("userId").default(UUID.fromString("00000000-0000-0000-0000-000000000000"))
    val content = blob("content")
}

class DbAsset(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbAsset>(DbAssets)

    var name by DbAssets.name
    var entries by DbAssets.entries
    var userId by DbAssets.userId
    var content by DbAssets.content

    fun toModel() = Asset(id.toString(), name, entries.split(","), userId.toString())

    fun fromModel(source: Asset) {
        name = source.name
        entries = source.entries.joinToString(",")
        userId = UUID.fromString(source.userId)
    }
}

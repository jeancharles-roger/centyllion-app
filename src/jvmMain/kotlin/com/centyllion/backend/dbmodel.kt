package com.centyllion.backend

import com.centyllion.model.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
import org.joda.time.DateTime
import java.util.*
import javax.sql.rowset.serial.SerialBlob

object DbUsers : UUIDTable("users") {
    val name = text("name")
    val keycloak = text("keycloak")
    val email = text("email")
}

class DbUser(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbUser>(DbUsers)

    var keycloak by DbUsers.keycloak
    var name by DbUsers.name
    var email by DbUsers.email

    fun toModel(): User = User(id.toString(), keycloak, name, email)

    fun fromModel(source: User) {
        keycloak = source.keycloakId
        name = source.name
        email = source.email
    }
}

object DbDescriptionInfos : UUIDTable("infoDescriptions") {
    val userId = uuid("userId")
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
        userId.toString(), createdOn.toString(), lastModifiedOn.toString(), readAccess, cloneAccess
    )

    fun fromModel(source: DescriptionInfo) {
        userId = UUID.fromString(source.userId)
        createdOn = DateTime.parse(source.createdOn)
        lastModifiedOn = DateTime.parse(source.lastModifiedOn)
        readAccess = source.readAccess
        cloneAccess = source.cloneAccess
    }
}

enum class DbModelType { Grain }

object DbModelDescriptions : UUIDTable("modelDescriptions") {
    val info = reference("info", DbDescriptionInfos)
    val model = text("model")
    val type = text("type")
    val version = integer("version")
}

class DbModelDescription(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbModelDescription>(DbModelDescriptions)

    var info by DbDescriptionInfo referencedOn DbModelDescriptions.info
    var model by DbModelDescriptions.model
    var type by DbModelDescriptions.type
    var version by DbModelDescriptions.version

    fun toModel(): GrainModelDescription {
        // TODO handle migrations
        val model = Json.parse(GrainModel.serializer(), model)
        return GrainModelDescription(id.toString(), info.toModel(), model)
    }

    fun fromModel(source: GrainModelDescription) {
        // TODO handle migrations
        info.fromModel(source.info)
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
                    modelModel.id, simulationModel.id, simulationModel.info.userId,
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
    val content = blob("content")
}

class DbAsset(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbAsset>(DbAssets)

    var name by DbAssets.name
    var content by DbAssets.content

    fun toModel() = Asset(
        id.toString(), name,
        content.getBytes(1, content.length().toInt())
    )

    fun fromModel(source: Asset) {
        name = source.name
        content = SerialBlob(source.data)
    }
}

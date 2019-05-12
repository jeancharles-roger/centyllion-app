package com.centyllion.backend

import com.centyllion.model.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
import org.joda.time.DateTime
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
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

object DbGrainModelDescriptions : UUIDTable("modelDescriptions") {
    val info = reference("info", DbDescriptionInfos)
    val model = text("model")
    val version = integer("version")
}

class DbGrainModelDescription(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbGrainModelDescription>(DbGrainModelDescriptions)

    var info by DbDescriptionInfo referencedOn DbGrainModelDescriptions.info
    var model by DbGrainModelDescriptions.model
    var version by DbGrainModelDescriptions.version

    fun toModel(): GrainModelDescription {
        val model = Json.parse(GrainModel.serializer(), model)
        return GrainModelDescription(id.toString(), info.toModel(), model)
    }

    fun fromModel(source: GrainModelDescription) {
        info.fromModel(source.info)
        model = Json.stringify(GrainModel.serializer(), source.model)
    }

}

object DbSimulations : UUIDTable("simulations") {
    val name = text("name")
    val description = text("description")
    val width = integer("width")
    val height = integer("height")
    val depth = integer("depth")
    val agents = blob("agents")
}

class DbSimulation(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbSimulation>(DbSimulations)

    var name by DbSimulations.name
    var description by DbSimulations.description
    var width by DbSimulations.width
    var height by DbSimulations.height
    var depth by DbSimulations.depth
    var agents by DbSimulations.agents

    fun toModel(): Simulation {
        // constructs the list of int from the blob
        var length = width * height * depth
        val stream = DataInputStream(agents.binaryStream)
        val list = mutableListOf<Int>()
        stream.use {
            try {
                while (true) {
                    list.add(it.readInt())
                }
            } catch (e: Throwable) {
                // end reading
            }
        }
        return Simulation(name, description, width, height, depth, list)
    }

    fun fromModel(source: Simulation) {
        name = source.name
        description = source.description
        width = source.width
        height = source.height
        depth = source.depth

        // writes the blob from the list of int
        val agentBlogSize = width * height * depth * Int.SIZE_BYTES
        val stream = ByteArrayOutputStream(agentBlogSize)
        DataOutputStream(stream).use { d -> source.agents.forEach { d.writeInt(it) } }
        agents = SerialBlob(stream.toByteArray())
    }
}

object DbSimulationDescriptions : UUIDTable("simulationDescriptions") {
    val info = reference("info", DbDescriptionInfos)
    val modelId = uuid("modelId")
    val thumbnailId = uuid("thumbnailId").nullable()
    val simulation = reference("simulation", DbSimulations)
}

class DbSimulationDescription(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DbSimulationDescription>(DbSimulationDescriptions)

    var info by DbDescriptionInfo referencedOn DbSimulationDescriptions.info
    var modelId by DbSimulationDescriptions.modelId
    var thumbnailId by DbSimulationDescriptions.thumbnailId
    var simulation by DbSimulation referencedOn DbSimulationDescriptions.simulation

    fun toModel(): SimulationDescription = SimulationDescription(
        id.toString(), info.toModel(), modelId.toString(), thumbnailId?.toString(), simulation.toModel()
    )

    fun fromModel(source: SimulationDescription) {
        info.fromModel(source.info)
        simulation.fromModel(source.simulation)
        modelId = UUID.fromString(source.modelId)
        thumbnailId = if (source.thumbnailId != null) UUID.fromString(source.thumbnailId) else null
    }

}

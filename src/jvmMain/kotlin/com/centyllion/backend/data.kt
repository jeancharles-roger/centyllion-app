package com.centyllion.backend

import com.centyllion.model.*
import io.ktor.auth.jwt.JWTPrincipal
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.*

interface Data {
    fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User
    fun getUser(id: String): User?
    fun saveUser(user: User)
    fun publicGrainModels(offset: Int = 0, limit: Int = 20): List<GrainModelDescription>
    fun grainModelsForUser(user: User): List<GrainModelDescription>
    fun getGrainModel(id: String): GrainModelDescription?
    fun createGrainModel(user: User, sent: GrainModel): GrainModelDescription
    fun saveGrainModel(user: User, model: GrainModelDescription)
    // TODO only pass model id
    fun deleteGrainModel(user: User, model: GrainModelDescription)

    fun getSimulationForModel(modelId: String): List<SimulationDescription>
    fun getSimulation(id: String): SimulationDescription?
    fun createSimulation(user: User, modelId: String, sent: Simulation): SimulationDescription
    fun saveSimulation(user: User, simulation: SimulationDescription)
    // TODO only pass simulation id
    fun deleteSimulation(user: User, simulation: SimulationDescription)

    fun getAllFeatured(): List<FeaturedDescription>
    fun getFeatured(id: String): FeaturedDescription?
    fun createFeatured(
        user: User,
        model: GrainModelDescription,
        simulation: SimulationDescription,
        author: User
    ): FeaturedDescription

    fun deleteFeatured(user: User, delete: FeaturedDescription)
    fun getAsset(id: String): Asset?
    fun createAsset(name: String, data: ByteArray): Asset
    fun deleteAsset(id: String)
    fun getEvents(): List<Event>
    fun insertEvent(action: Action, user: User?, collection: String, vararg arguments: String)
}

val rfc1123Format = SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z", Locale.US)

fun newId() = UUID.randomUUID().toString()

fun createGrainModelDescription(user: User, sent: GrainModel) = rfc1123Format.format(Date()).let {
    GrainModelDescription(newId(), DescriptionInfo(user.id, it, it, false, false), sent)
}

fun createFeaturedDescription(
    asset: Asset, model: GrainModelDescription, simulation: SimulationDescription, author: User
) = FeaturedDescription(
    newId(), rfc1123Format.format(Date()), asset.id,
    model.id, simulation.id, author.id,
    listOf(simulation.simulation.name, model.model.name).filter { it.isNotEmpty() }.joinToString(" / "),
    listOf(simulation.simulation.description, model.model.description).filter { it.isNotEmpty() }.joinToString("\n"),
    author.name, model.model.grains.map { it.color }
)

fun createEvent(
    action: Action,
    user: User?,
    collection: String,
    arguments: Array<out String>
): Event {
    val date = rfc1123Format.format(Date())
    return Event(newId(), date, user?.id ?: "", action, collection, arguments.toList())
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

    val database = Database.connect({ DriverManager.getConnection(url, user, password) })

    init {
        transaction(database) {
            SchemaUtils.create(
                DbUsers, DbDescriptionInfos,
                DbGrainModelDescriptions,
                DbSimulations, DbSimulationDescriptions
            )
        }
    }

    val featured: LinkedHashMap<String, FeaturedDescription> = linkedMapOf()
    val assets: LinkedHashMap<String, Asset> = linkedMapOf()
    val events: LinkedHashMap<String, Event> = linkedMapOf()

    override fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User {
        val user = transaction(database) {
            DbUser.find { DbUsers.keycloak eq principal.payload.subject }.firstOrNull()
        } ?: principal.payload.claims.let { claims ->
            transaction(database) {
                DbUser.new {
                    keycloak = principal.payload.subject
                    name = claims["name"]?.asString() ?: ""
                    email = claims["email"]?.asString() ?: ""
                }
            }
        }
        return user.toModel()
    }

    override fun getUser(id: String): User? = transaction(database) { DbUser.findById(UUID.fromString(id))?.toModel() }

    override fun saveUser(user: User) {
        transaction(database) { DbUser.findById(UUID.fromString(user.id))?.fromModel(user) }
    }

    override fun publicGrainModels(offset: Int, limit: Int) = transaction(database) {
        DbGrainModelDescription.all().limit(limit, offset).map { it.toModel() }
    }

    override fun grainModelsForUser(user: User): List<GrainModelDescription> = transaction(database) {
        val userUUID = UUID.fromString(user.id)
        DbGrainModelDescription.wrapRows(
            DbGrainModelDescriptions
                .innerJoin(DbDescriptionInfos)
                .select { DbDescriptionInfos.userId eq userUUID }
        ).map { it.toModel() }
    }

    override fun getGrainModel(id: String) = transaction(database) {
        DbGrainModelDescription.findById(UUID.fromString(id))?.toModel()
    }

    override fun createGrainModel(user: User, sent: GrainModel): GrainModelDescription = transaction(database) {
        val newInfo = DbDescriptionInfo.new {
            userId = UUID.fromString(user.id)
            createdOn = DateTime.now()
            lastModifiedOn = DateTime.now()
            readAccess = false
            cloneAccess = false
        }
        DbGrainModelDescription.new {
            info = newInfo
            model = Json.stringify(GrainModel.serializer(), sent)
            version = 0
        }.toModel()
    }

    override fun saveGrainModel(user: User, model: GrainModelDescription) {
        transaction(database) { DbGrainModelDescription.findById(UUID.fromString(model.id))?.fromModel(model) }
    }

    override fun deleteGrainModel(user: User, model: GrainModelDescription) {
        transaction(database) {
            DbSimulationDescription
                .find { DbSimulationDescriptions.modelId eq UUID.fromString(model.id) }
                .forEach { it.delete() }
            DbGrainModelDescription.findById(UUID.fromString(model.id))?.delete()
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
            val newSimulation = DbSimulation.new { fromModel(sent) }
            DbSimulationDescription.new {
                info = newInfo
                this.modelId = UUID.fromString(modelId)
                this.simulation = newSimulation
            }.toModel()
        }

    override fun saveSimulation(user: User, simulation: SimulationDescription) {
        transaction(database) {
            DbSimulationDescription.findById(UUID.fromString(simulation.id))?.fromModel(simulation)
        }
    }

    override fun deleteSimulation(user: User, simulation: SimulationDescription) {
        transaction(database) { DbSimulationDescription.findById(UUID.fromString(simulation.id))?.delete() }
    }

    override fun getAllFeatured(): List<FeaturedDescription> = featured.values.toList()

    override fun getFeatured(id: String) = featured[id]

    override fun createFeatured(
        user: User, model: GrainModelDescription, simulation: SimulationDescription, author: User
    ): FeaturedDescription {
        val asset = createAsset("simulation.png", createThumbnail(model.model, simulation.simulation))
        val new = createFeaturedDescription(asset, model, simulation, author)
        featured[new.id] = new
        return new
    }

    override fun deleteFeatured(user: User, delete: FeaturedDescription) {
        // delete thumbnail asset
        if (delete.thumbnailId.isNotEmpty()) deleteAsset(delete.thumbnailId)
        // delete the featured
        featured.remove(delete.id)
    }

    override fun getAsset(id: String) = assets[id]

    override fun createAsset(name: String, data: ByteArray): Asset {
        val id = newId()
        val result = Asset(id, name, data)
        assets[id] = result
        return result
    }

    override fun deleteAsset(id: String) {
        assets.remove(id)
    }

    override fun getEvents() = events.values.toList()

    override fun insertEvent(action: Action, user: User?, collection: String, vararg arguments: String) {
        val event = createEvent(action, user, collection, arguments)
        events[event.id] = event
    }
}


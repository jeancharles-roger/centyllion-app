package com.centyllion.backend

import com.centyllion.model.*
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import io.ktor.auth.jwt.JWTPrincipal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.Binary
import org.bson.types.ObjectId
import org.litote.kmongo.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.toList

interface Data {
    fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User
    fun getUser(id: String): User?
    fun saveUser(user: User)
    fun publicGrainModels(max: Int = 20): List<GrainModelDescription>
    fun grainModelsForUser(user: User): List<GrainModelDescription>
    fun getGrainModel(id: String): GrainModelDescription?
    fun createGrainModel(user: User, sent: GrainModel): GrainModelDescription
    fun saveGrainModel(user: User, model: GrainModelDescription)
    fun deleteGrainModel(user: User, model: GrainModelDescription)
    fun getSimulationForModel(modelId: String): List<SimulationDescription>
    fun getSimulation(id: String): SimulationDescription?
    fun createSimulation(user: User, modelId: String, sent: Simulation): SimulationDescription
    fun saveSimulation(user: User, simulation: SimulationDescription)
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

private val rfc1123Format = SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z", Locale.US)

fun createUser(principal: JWTPrincipal, keycloakId: String): User {
    val claims = principal.payload.claims
    val name = claims["name"]?.asString() ?: ""
    val email = claims["email"]?.asString() ?: ""
    val new = User(newId<User>().toString(), keycloakId, name, email)
    return new
}

fun createGrainModelDescription(user: User, sent: GrainModel) = GrainModelDescription(
    newId<GrainModelDescription>().toString(),
    DescriptionInfo(user._id, null, null, rfc1123Format.format(Date())),
    sent
)

fun createSimulationDescription(user: User, modelId: String, sent: Simulation) = SimulationDescription(
    newId<SimulationDescription>().toString(),
    DescriptionInfo(user._id, null, null, rfc1123Format.format(Date())),
    modelId, sent
)

fun createFeaturedDescription(
    asset: Asset, model: GrainModelDescription, simulation: SimulationDescription, author: User
) = FeaturedDescription(
    newId<SimulationDescription>().toString(), rfc1123Format.format(Date()), asset._id,
    model._id, simulation._id, author._id,
    listOf(simulation.simulation.name, model.model.name).filter { it.isNotEmpty() }.joinToString(" / "),
    listOf(simulation.simulation.description, model.model.description).filter { it.isNotEmpty() }.joinToString("\n"),
    author.name, model.model.grains.map { it.color }
)

fun createEvent(action: Action, user: User?, collection: String, arguments: Array<out String>): Event {
    val date = rfc1123Format.format(Date())
    return Event(newId<Event>().toString(), date, user?._id ?: "", action, collection, arguments.toList())
}

class MongoData(
    host: String = "localhost",
    port: Int = 27017,
    dbName: String = "centyllion"
) : Data {
    val client = MongoClient(host, port)

    val db: MongoDatabase = client.getDatabase(dbName)

    val usersCollectionName = "users"
    val grainModelsCollectionName = "grainModels"
    val simulationsCollectionName = "simulations"
    val featuredCollectionName = "featured"
    val assetsCollectionName = "assets"
    val eventsCollectionName = "events"

    val users: MongoCollection<Document> = db.getCollection(usersCollectionName)
    val grainModels: MongoCollection<Document> = db.getCollection(grainModelsCollectionName)
    val simulations: MongoCollection<Document> = db.getCollection(simulationsCollectionName)
    val featured: MongoCollection<Document> = db.getCollection(featuredCollectionName)
    val assets: MongoCollection<Document> = db.getCollection(assetsCollectionName)
    val events: MongoCollection<Document> = db.getCollection(eventsCollectionName)

    override fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User {
        val keycloakId = principal.payload.subject
        // tries to find user without lock
        return users.findOne("{keycloakId: '$keycloakId'}").let {
            if (it == null) {
                // no user found, lock on the db client to avoid multiple user creation
                synchronized(db) {
                    // inside the lock, tries again to find the user
                    users.findOne("{keycloakId: '$keycloakId'}").let {
                        if (it == null) {
                            // creates new user from claims if non existent
                            val new = createUser(principal, keycloakId)
                            users.insertOne(createDocument(User.serializer(), new))
                            insertEvent(Action.Create, new, usersCollectionName, new.name)
                            new
                        } else {
                            // user was created
                            parseDocument(User.serializer(), it)
                        }
                    }
                }
            } else {
                // user exists
                parseDocument(User.serializer(), it)
            }
        }
    }

    override fun getUser(id: String): User? {
        val result = users.findOneById(id)
        return result?.let { parseDocument(User.serializer(), result) }
    }

    override fun saveUser(user: User) {
        val document = createDocument(User.serializer(), user)
        users.save(document)
        insertEvent(Action.Save, user, usersCollectionName, user.name)
    }

    override fun publicGrainModels(max: Int): List<GrainModelDescription> {
        val result = grainModels.find("{'info.access': ['Read']}").limit(max)
        return result.map { parseDocument(GrainModelDescription.serializer(), it) }.toList()
    }

    override fun grainModelsForUser(user: User): List<GrainModelDescription> {
        val result = grainModels.find("{'info.userId': '${user._id}'}")
        return result.map { parseDocument(GrainModelDescription.serializer(), it) }.toList()
    }

    override fun getGrainModel(id: String): GrainModelDescription? {
        val result = grainModels.findOneById(id)
        return result?.let { parseDocument(GrainModelDescription.serializer(), result) }
    }

    override fun createGrainModel(user: User, sent: GrainModel): GrainModelDescription {
        val model = createGrainModelDescription(user, sent)
        grainModels.save(createDocument(GrainModelDescription.serializer(), model))
        insertEvent(Action.Create, user, grainModelsCollectionName, model._id, model.model.name)
        return model
    }

    override fun saveGrainModel(user: User, model: GrainModelDescription) {
        grainModels.save(createDocument(GrainModelDescription.serializer(), model))
        insertEvent(Action.Save, user, grainModelsCollectionName, model._id)
    }

    override fun deleteGrainModel(user: User, model: GrainModelDescription) {
        // first delete all simulation for model
        getSimulationForModel(model._id).forEach { deleteSimulation(user, it) }
        // delete the model
        grainModels.deleteOneById(model._id)
        insertEvent(Action.Delete, user, grainModelsCollectionName, model._id)
    }

    override fun getSimulationForModel(modelId: String): List<SimulationDescription> {
        val result = simulations.find("{modelId: '$modelId'}")
        return result.map { parseDocument(SimulationDescription.serializer(), it) }.toList()
    }

    override fun getSimulation(id: String): SimulationDescription? {
        val result = simulations.findOneById(id)
        return result?.let { parseDocument(SimulationDescription.serializer(), result) }
    }

    override fun createSimulation(user: User, modelId: String, sent: Simulation): SimulationDescription {
        val simulation = createSimulationDescription(user, modelId, sent)
        simulations.save(createDocument(SimulationDescription.serializer(), simulation))
        insertEvent(Action.Create, user, simulationsCollectionName, simulation._id)
        return simulation
    }

    override fun saveSimulation(user: User, simulation: SimulationDescription) {
        simulations.save(createDocument(SimulationDescription.serializer(), simulation))
        insertEvent(Action.Save, user, simulationsCollectionName, simulation._id)
    }

    override fun deleteSimulation(user: User, simulation: SimulationDescription) {
        simulations.deleteOneById(simulation._id)
        insertEvent(Action.Delete, user, simulationsCollectionName, simulation._id)
    }

    override fun getAllFeatured(): List<FeaturedDescription> {
        val result = featured.find()
        return result.map { parseDocument(FeaturedDescription.serializer(), it) }.toList()
    }

    override fun getFeatured(id: String): FeaturedDescription? {
        val result = featured.findOneById(id)
        return result?.let { parseDocument(FeaturedDescription.serializer(), result) }
    }

    override fun createFeatured(
        user: User, model: GrainModelDescription, simulation: SimulationDescription, author: User
    ): FeaturedDescription {
        val asset = createAsset("simulation.png", createThumbnail(model.model, simulation.simulation))
        val new = createFeaturedDescription(asset, model, simulation, author)

        val document = createDocument(FeaturedDescription.serializer(), new)
        featured.save(document)
        insertEvent(Action.Create, user, featuredCollectionName, new._id, new.name)
        return new
    }

    override fun deleteFeatured(user: User, delete: FeaturedDescription) {
        // delete thumbnail asset
        if (delete.thumbnailId.isNotEmpty()) deleteAsset(delete.thumbnailId)
        // delete the featured
        featured.deleteOneById(delete._id)
        insertEvent(Action.Delete, user, featuredCollectionName, delete._id)
    }

    override fun getAsset(id: String): Asset? {
        val result = assets.findOneById(ObjectId(id))
        return result?.let {
            val name = it.getString("name")
            val data = it.get("data", Binary::class.java)
            Asset(id, name, data.data)
        }
    }

    override fun createAsset(name: String, data: ByteArray): Asset {
        val id = newId<Asset>().toString()
        val document = Document("_id", ObjectId(id))
        document.append("name", name)
        document.append("data", data)
        assets.save(document)
        return Asset(id, name, data)
    }

    override fun deleteAsset(id: String) {
        assets.deleteOneById(ObjectId(id))
    }

    override fun getEvents(): List<Event> {
        val result = events.find()
        return result.reversed().map {
            parseDocument(Event.serializer(), it)
        }.toList()
    }

    override fun insertEvent(action: Action, user: User?, collection: String, vararg arguments: String) {
        val event = createEvent(action, user, collection, arguments)
        events.insertOne(createDocument(Event.serializer(), event))
    }

    private fun <T> parseDocument(serializer: KSerializer<T>, document: Document): T {
        return Json.parse(serializer, migrate(serializer, document).toJson())
    }

    private fun <T> createDocument(serializer: KSerializer<T>, value: T) =
        Document.parse(Json.stringify(serializer, value))
}

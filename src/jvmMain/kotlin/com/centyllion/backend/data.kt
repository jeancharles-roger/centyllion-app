package com.centyllion.backend

import com.centyllion.model.*
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import io.ktor.auth.jwt.JWTPrincipal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.bson.Document
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.newId
import org.litote.kmongo.save
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.toList

class Data(
    host: String = "localhost",
    port: Int = 27017,
    dbName: String = "centyllion"
) {
    val client = MongoClient(host, port)

    val db: MongoDatabase = client.getDatabase(dbName)

    val usersCollectionName = "users"
    val grainModelsCollectionName = "grainModels"
    val simulationsCollectionName = "simulations"
    val eventsCollectionName = "events"

    val users: MongoCollection<Document> = db.getCollection(usersCollectionName)

    val grainModels: MongoCollection<Document> = db.getCollection(grainModelsCollectionName)

    val simulations: MongoCollection<Document> = db.getCollection(simulationsCollectionName)

    val events: MongoCollection<Document> = db.getCollection(eventsCollectionName)

    private val rfc1123Format = SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z", Locale.US)

    fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User {
        val id = principal.payload.subject
        // tries to find user without lock
        return users.findOne("{keycloakId: '$id'}").let {
            if (it == null) {
                // no user found, lock on the db client to avoid multiple user creation
                synchronized(db) {
                    // inside the lock, tries again to find the user
                    users.findOne("{keycloakId: '$id'}").let {
                        if (it == null) {
                            // creates new user from claims if non existent
                            val claims = principal.payload.claims
                            val name = claims["name"]?.asString() ?: ""
                            val email = claims["email"]?.asString() ?: ""
                            val new = User(newId<User>().toString(), userVersion, id, name, email)
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

    fun saveUser(user: User) {
        val document = createDocument(User.serializer(), user)
        users.save(document)
        insertEvent(Action.Save, user, usersCollectionName, user.name)
    }

    fun publicGrainModels(max: Int = 20): List<GrainModelDescription> {
        val result = grainModels.find("{'info.access': ['Read']}").limit(max)
        return result.map { parseDocument(GrainModelDescription.serializer(), it) }.toList()
    }

    fun grainModelsForUser(user: User): List<GrainModelDescription> {
        val result = grainModels.find("{'info.userId': '${user._id}'}")
        return result.map { parseDocument(GrainModelDescription.serializer(), it) }.toList()
    }

    fun getGrainModel(id: String): GrainModelDescription? {
        val result = grainModels.findOne("{_id: '$id'}")
        return result?.let { parseDocument(GrainModelDescription.serializer(), result) }
    }

    fun createGrainModel(user: User, sent: GrainModel): GrainModelDescription {
        val date = rfc1123Format.format(Date())
        val model = GrainModelDescription(
            newId<GrainModelDescription>().toString(), grainModelVersion,
            DescriptionInfo(user._id, null, null, date),
            sent
        )
        grainModels.save(createDocument(GrainModelDescription.serializer(), model))
        insertEvent(Action.Create, user, grainModelsCollectionName, model._id, model.model.name)
        return model
    }

    fun saveGrainModel(user: User, model: GrainModelDescription) {
        grainModels.save(createDocument(GrainModelDescription.serializer(), model))
        insertEvent(Action.Save, user, grainModelsCollectionName, model._id)
    }

    fun deleteGrainModel(user: User, model: GrainModelDescription) {
        // first delete all simulation for model
        getSimulationForModel(model._id).forEach{ deleteSimulation(user, it) }
        // delete the model
        grainModels.deleteOneById(model._id)
        insertEvent(Action.Delete, user, grainModelsCollectionName, model._id)
    }

    fun getSimulationForModel(modelId: String): List<SimulationDescription> {
        val result = simulations.find("{modelId: '$modelId'}")
        return result.map { parseDocument(SimulationDescription.serializer(), it) }.toList()
    }

    fun getSimulation(id: String): SimulationDescription? {
        val result = simulations.findOne("{_id: '$id'}")
        return result?.let { parseDocument(SimulationDescription.serializer(), result) }
    }

    fun createSimulation(user: User, modelId: String, sent: Simulation): SimulationDescription {
        val date = rfc1123Format.format(Date())
        val simulation = SimulationDescription(
            newId<SimulationDescription>().toString(), simulationVersion,
            DescriptionInfo(user._id, null, null, date),
            modelId, sent
        )
        simulations.save(createDocument(SimulationDescription.serializer(), simulation))
        insertEvent(Action.Create, user, simulationsCollectionName, simulation._id)
        return simulation
    }

    fun saveSimulation(user: User, simulation: SimulationDescription) {
        simulations.save(createDocument(SimulationDescription.serializer(), simulation))
        insertEvent(Action.Save, user, simulationsCollectionName, simulation._id)
    }

    fun deleteSimulation(user: User, simulation: SimulationDescription) {
        simulations.deleteOneById(simulation._id)
        insertEvent(Action.Delete, user,  simulationsCollectionName, simulation._id)
    }

    fun getEvents(): List<Event> {
        val result = events.find()
        return result.reversed().map {
            parseDocument(Event.serializer(), it)
        }.toList()
    }

    fun insertEvent(action: Action, user: User?, collection: String, vararg arguments: String) {
        val date = rfc1123Format.format(Date())
        val event = Event(
            newId<Event>().toString(), eventVersion, date,
            user?._id ?: "", action, collection, arguments.toList()
         )
        events.insertOne(createDocument(Event.serializer(), event))
    }

    private fun <T> parseDocument(serializer: KSerializer<T>, document: Document): T {
        return Json.parse(serializer, migrate(serializer, document).toJson())
    }

    private fun <T> createDocument(serializer: KSerializer<T>, value: T) =
        Document.parse(Json.stringify(serializer, value))
}

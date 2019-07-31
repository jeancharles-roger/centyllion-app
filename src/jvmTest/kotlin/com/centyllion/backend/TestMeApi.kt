package com.centyllion.backend

import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.User
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@KtorExperimentalAPI
class TestMeApi {

    private fun TestApplicationEngine.postModel(model: GrainModel, user: User): GrainModelDescription {
        val content = Json.stringify(GrainModel.serializer(), model)
        val request = handlePost("/api/model", content, user)
        assertEquals(HttpStatusCode.OK, request.response.status())

        val result = request.response.content
        assertNotNull(result)

        val retrieved = Json.parse(GrainModelDescription.serializer(), result)
        assertEquals(model, retrieved.model)
        return retrieved
    }

    private fun TestApplicationEngine.deleteModel(
        model: GrainModelDescription, user: User, result: HttpStatusCode = HttpStatusCode.OK
    ) {
        val request = handleDelete("/api/model/${model.id}", user)
        assertEquals(result, request.response.status())
    }

    private fun TestApplicationEngine.patchModel(
        model: GrainModelDescription, user: User, result: HttpStatusCode = HttpStatusCode.OK
    ) {
        val content = Json.stringify(GrainModelDescription.serializer(), model)
        val request = handlePatch("/api/model/${model.id}", content, user)
        assertEquals(result, request.response.status())
    }

    @Test
    fun testRequestMe() = withCentyllion {
        // Test that /api/me is protected
        testUnauthorized("/api/me")

        // Test that /api/me returns the created user the first time, and the same user the second time.
        repeat(2) {
            with(handleGet("/api/me", apprenticeUser)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.contentType().match(ContentType.Application.Json))

                val user = Json.parse(User.serializer(), response.content ?: "")
                assertEquals(apprenticeUser.name, user.name)
                assertEquals(apprenticeUser.details?.email, user.details?.email)
                assertEquals(apprenticeUser.details?.keycloakId, user.details?.keycloakId)
            }
        }
    }

    @Test
    fun testMyModelsApprentice() = withCentyllion {
        // Test that /api/me/model is protected
        testUnauthorized( "/api/me/model")

        // Test that /api/me/model is protected by the apprentice model
        testGet("/api/me/model", emptyList(), GrainModelDescription.serializer().list, apprenticeUser)

        // Test post on /api/model
        testUnauthorized("/api/me/model", HttpMethod.Post)
        val model1 = postModel(GrainModel("test1"), apprenticeUser)
        val model2 = postModel(GrainModel("test2"), apprenticeUser)

        // Checks that models were posted
        testGet("/api/me/model", listOf(model1, model2), GrainModelDescription.serializer().list, apprenticeUser)

        // Test delete a model
        testUnauthorized("/api/me/model/${model1.id}", HttpMethod.Delete)
        deleteModel(model1, apprenticeUser)

        // Checks if delete happened
        testGet("/api/me/model", listOf(model2), GrainModelDescription.serializer().list, apprenticeUser)

        // Test patch
        val newModel2 = model2.copy(model = model2.model.copy("Test 2 bis"))
        testUnauthorized("/api/me/model/${model2.id}", HttpMethod.Patch)
        patchModel(newModel2, apprenticeUser)

        // Checks if patch happened
        testGet("/api/me/model", listOf(newModel2), GrainModelDescription.serializer().list, apprenticeUser)

        // Checks that publish is protected by creator role
        val newModel2public = model2.copy(info = model2.info.copy(readAccess = true))
        patchModel(newModel2public, apprenticeUser, HttpStatusCode.Forbidden)
        patchModel(newModel2public, creatorUser, HttpStatusCode.Unauthorized)
    }

    @Test
    fun testPublishMyModelsCreator() = withCentyllion {
        // Creates and make a model public with creator user
        val model3 = postModel(GrainModel("test3"), creatorUser)
        val model3public = model3.copy(info = model3.info.copy(readAccess = true))
        patchModel(model3public, creatorUser)
    }
}

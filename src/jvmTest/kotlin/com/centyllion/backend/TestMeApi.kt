package com.centyllion.backend

import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.User
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestMeApi {

    private fun TestApplicationEngine.getMe(user: User): User {
        val request = handleGet("/api/me", user)
        assertEquals(HttpStatusCode.OK, request.response.status())

        val result = request.response.content
        assertNotNull(result)

        return Json.decodeFromString(User.serializer(), result)
    }

    private fun TestApplicationEngine.postModel(model: GrainModel, user: User): GrainModelDescription {
        val content = Json.encodeToString(GrainModel.serializer(), model)
        val request = handlePost("/api/model", content, user)
        assertEquals(HttpStatusCode.OK, request.response.status())

        val result = request.response.content
        assertNotNull(result)

        val retrieved = Json.decodeFromString(GrainModelDescription.serializer(), result)
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
        val content = Json.encodeToString(GrainModelDescription.serializer(), model)
        val request = handlePatch("/api/model/${model.id}", content, user)
        assertEquals(result, request.response.status())
    }

    @Test
    fun testRequestMe() = withCentyllion {
        // Test that /api/me is protected
        testUnauthorized("/api/me")

        // Test that /api/me returns the created user the first time, and the same user the second time.
        repeat(2) {
            with(handleGet("/api/me", user1)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.contentType().match(ContentType.Application.Json))

                val user = Json.decodeFromString(User.serializer(), response.content ?: "")
                assertEquals(user1.name, user.name)
                assertEquals(user1.details?.email, user.details?.email)
                assertEquals(user1.details?.keycloakId, user.details?.keycloakId)
            }
        }
    }

    @Test
    fun testMyModels1() = withCentyllion {
        // Test that /api/me/model is protected
        testUnauthorized("/api/me/model")

        // Test that /api/me/model is protected by the apprentice model
        testGetPage("/api/me/model", emptyList(), 0, GrainModelDescription.serializer(), user1)

        // Test post on /api/model
        testUnauthorized("/api/me/model", HttpMethod.Post)
        val model1 = postModel(GrainModel("test1"), user1)
        val model2 = postModel(GrainModel("test2"), user1)
        patchModel(model2, user2, HttpStatusCode.Unauthorized)

        // Checks that models were posted
        testGetPage("/api/me/model", listOf(model1, model2), 2, GrainModelDescription.serializer(), user1)

        // Test delete a model
        testUnauthorized("/api/me/model/${model1.id}", HttpMethod.Delete)
        deleteModel(model1, user1)

        // Checks if delete happened
        testGetPage("/api/me/model", listOf(model2), 1, GrainModelDescription.serializer(), user1)

        // Test patch
        val newModel2 = model2.copy(model = model2.model.copy("Test 2 bis"))
        testUnauthorized("/api/me/model/${model2.id}", HttpMethod.Patch)
        patchModel(newModel2, user1)

        // Checks if patch happened
        testGetPage("/api/me/model", listOf(newModel2), 1, GrainModelDescription.serializer(), user1)

        // Tests private model
        val privateModel2 = model2.copy(info = model2.info.copy(readAccess = false))
        patchModel(privateModel2, user1, HttpStatusCode.Forbidden)
    }
}

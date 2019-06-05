package com.centyllion.backend

import com.centyllion.common.creatorRole
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

    private fun TestApplicationEngine.postModel(model: GrainModel, user: User, vararg role: String): GrainModelDescription {
        val content = Json.stringify(GrainModel.serializer(), model)
        val request = handleLoggedInPost("/api/model", content, user, *role)
        assertEquals(HttpStatusCode.OK, request.response.status())

        val result = request.response.content
        assertNotNull(result)

        val retrieved = Json.parse(GrainModelDescription.serializer(), result)
        assertEquals(model, retrieved.model)
        return retrieved
    }

    private fun TestApplicationEngine.deleteModel(model: GrainModelDescription, user: User, vararg role: String) {
        val request = handleLoggedInDelete("/api/model/${model.id}", user, *role)
        assertEquals(HttpStatusCode.OK, request.response.status())
    }

    private fun TestApplicationEngine.patchModel(model: GrainModelDescription, user: User, vararg role: String) {
        val content = Json.stringify(GrainModelDescription.serializer(), model)
        val request = handleLoggedInPatch("/api/model/${model.id}", content, user, *role)
        assertEquals(HttpStatusCode.OK, request.response.status())
    }

    @Test
    fun testRequestMe() = withCentyllion {
        // Test that /api/me is protected
        testUnauthorized("/api/me")

        // Test that /api/me returns the created user the first time, and the same user the second time.
        repeat(2) {
            with(handleLoggedInGet("/api/me", testUser)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.contentType().match(ContentType.Application.Json))

                val user = Json.parse(User.serializer(), response.content ?: "")
                assertEquals(testUser.name, user.name)
                assertEquals(testUser.details?.email, user.details?.email)
                assertEquals(testUser.details?.keycloakId, user.details?.keycloakId)
            }
        }
    }

    @Test
    fun testMyModels() = withCentyllion {
        // Test that /api/me/model is protected
        testUnauthorized( "/api/me/model")

        // Test that /api/me/model is protected by the role model
        testGet("/api/me/model", emptyList(), GrainModelDescription.serializer().list, testUser)

        // Test post on /api/model
        testUnauthorized("/api/me/model", HttpMethod.Post)
        testUnauthorized("/api/me/model", HttpMethod.Post, testUser)
        val model1 = postModel(GrainModel("test1"), testUser, creatorRole)
        val model2 = postModel(GrainModel("test2"), testUser, creatorRole)

        // Checks that models were posted
        testGet("/api/me/model", listOf(model1, model2), GrainModelDescription.serializer().list, testUser, creatorRole)

        // Test delete a model
        testUnauthorized("/api/me/model/${model1.id}", HttpMethod.Delete)
        testUnauthorized("/api/me/model/${model1.id}", HttpMethod.Delete, testUser)
        deleteModel(model1, testUser, creatorRole)

        // Checks if delete happened
        testGet("/api/me/model", listOf(model2), GrainModelDescription.serializer().list, testUser, creatorRole)

        // Test patch
        val newModel2 = model2.copy(model = model2.model.copy("Test 2 bis"))
        testUnauthorized("/api/me/model/${model2.id}", HttpMethod.Patch)
        testUnauthorized("/api/me/model/${model2.id}", HttpMethod.Patch, testUser)
        patchModel(newModel2, testUser, creatorRole)

        // Checks if patch happened
        testGet("/api/me/model", listOf(newModel2), GrainModelDescription.serializer().list, testUser, creatorRole)

    }

}

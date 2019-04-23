package com.centyllion.backend

import com.centyllion.common.modelRole
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.User
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalAPI
class TestModelApi {

    val testUser = User("1", "1234", "Test", "test@centyllion.com")

    @Test
    fun testRequestModels() = withCentyllion {
        with(handleRequest(HttpMethod.Get, "/api/model")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val models = Json.parse(GrainModelDescription.serializer().list, response.content ?: "")
            assertEquals(0, models.size)
        }
    }

    @Test
    fun testRequestMe() = withCentyllion {
        // Test that /api/me is protected
        with(handleRequest(HttpMethod.Get, "/api/me")) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }

        // Test that /api/me returns the created user the first time, and the same user the second time.
        repeat(2) {
            with(handleLoggedInRequest(HttpMethod.Get, "/api/me", testUser)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.contentType().match(ContentType.Application.Json))

                val user = Json.parse(User.serializer(), response.content ?: "")
                assertEquals(testUser.name, user.name)
                assertEquals(testUser.email, user.email)
                assertEquals(testUser.keycloakId, user.keycloakId)
            }
        }
    }

    @Test
    fun testMyModels() = withCentyllion {
        // Test that /api/me/model is protected
        with(handleRequest(HttpMethod.Get, "/api/me/model")) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }

        // Test that /api/me/model is protected by a role
        with(handleLoggedInRequest(HttpMethod.Get, "/api/me/model", testUser)) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }

        // Test that /api/me/model is protected by the role model
        with(handleLoggedInRequest(HttpMethod.Get, "/api/me/model", testUser, modelRole)) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val models = Json.parse(GrainModelDescription.serializer().list, response.content ?: "")
            assertEquals(0, models.size)
        }
    }


}

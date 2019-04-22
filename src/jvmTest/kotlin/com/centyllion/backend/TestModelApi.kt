package com.centyllion.backend

import com.centyllion.model.GrainModelDescription
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalAPI
class TestModelApi {

    @Test
    fun testRequestModels() = withTestApplication({ centyllion(false, MemoryData()) }) {
        with(handleRequest(HttpMethod.Get, "/api/model")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val models = Json.parse(GrainModelDescription.serializer().list, response.content ?: "")
            assertEquals(0, models.size)
        }
    }
}

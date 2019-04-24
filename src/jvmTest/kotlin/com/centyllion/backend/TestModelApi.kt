package com.centyllion.backend

import com.centyllion.model.GrainModelDescription
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.list
import kotlin.test.Test

@KtorExperimentalAPI
class TestModelApi {

    @Test
    fun testRequestModels() = withCentyllion {
        testGet("/api/model", emptyList(), GrainModelDescription.serializer().list)
    }

}

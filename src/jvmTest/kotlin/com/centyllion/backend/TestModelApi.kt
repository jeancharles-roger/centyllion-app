package com.centyllion.backend

import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ResultPage
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.Test

@KtorExperimentalAPI
class TestModelApi {

    @Test
    fun testRequestModels() = withCentyllion {
        testGet(
            "/api/model", ResultPage(emptyList(), 0, 0),
            ResultPage.serializer(GrainModelDescription.serializer())
        )
    }

}

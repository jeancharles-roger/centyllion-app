package com.centyllion.backend

import com.centyllion.model.ResultPage
import com.centyllion.model.User
import com.centyllion.model.emptyResultPage
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.Test


@KtorExperimentalAPI
class TestUsersApi {

    @Test
    fun testRequestUsers() = withCentyllion {
        // Test get users
        testGet("/api/user", emptyResultPage(), ResultPage.serializer(User.serializer()))

        // Test that /api/user?details=true is protected
        testUnauthorized("/api/user?detailed=true")

        // create a user
        val user = get("/api/me", User.serializer(), apprenticeUser)

        // check users
        val result = ResultPage(listOf(user.copy(details = null)), 0, 1)
        testGet("/api/user", result, ResultPage.serializer(User.serializer()))

    }
}

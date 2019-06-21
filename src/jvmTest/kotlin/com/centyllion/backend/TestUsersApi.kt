package com.centyllion.backend

import com.centyllion.common.SubscriptionType
import com.centyllion.model.ResultPage
import com.centyllion.model.Subscription
import com.centyllion.model.SubscriptionParameters
import com.centyllion.model.SubscriptionState
import com.centyllion.model.User
import com.centyllion.model.emptyResultPage
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.list
import kotlin.test.Test
import kotlin.test.assertEquals


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

    @Test
    fun testSubscription() = withCentyllion {
        // creates a user
        val user = get("/api/me", User.serializer(), apprenticeUser)

        // Tests that /api/user/${user.id}/subscription is protected
        testUnauthorized("/api/user/${user.id}/subscription")
        testUnauthorized("/api/user/${user.id}/subscription", user = apprenticeUser)
        testUnauthorized("/api/user/${user.id}/subscription", user = creatorUser)

        // tests subscription list
        testGet("/api/user/${user.id}/subscription", emptyList(), Subscription.serializer().list, adminUser)

        val parameters = SubscriptionParameters(false, SubscriptionType.Creator, 10, 0.0, "manual")
        val subscription = testPost(
            "/api/user/${user.id}/subscription", parameters, SubscriptionParameters.serializer(),
            Subscription.serializer(), adminUser
        )

        assertEquals(SubscriptionState.Engaged, subscription.state)
        testGet("/api/user/${user.id}/subscription", listOf(subscription), Subscription.serializer().list, adminUser)

        // retrieves updated user to check is subscription is engaged
        val updatedUser = get("/api/me", User.serializer(), apprenticeUser)
        assertEquals(SubscriptionType.Creator, updatedUser.details?.subscription)

        // Waits for the subscription to end
        Thread.sleep(500)

        // retrieves user to check update subscription
        val updatedUser2 = get("/api/me", User.serializer(), apprenticeUser)
        assertEquals(SubscriptionType.Apprentice, updatedUser2.details?.subscription)

        // checks that subscription is disengaged
        val expectedSubscription = subscription.copy(state = SubscriptionState.Disengaged)
        testGet(
            "/api/user/${user.id}/subscription/${subscription.id}", expectedSubscription,
            Subscription.serializer(), adminUser
        )

    }
}

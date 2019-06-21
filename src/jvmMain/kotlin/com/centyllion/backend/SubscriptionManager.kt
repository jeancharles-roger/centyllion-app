package com.centyllion.backend

import com.centyllion.common.SubscriptionType
import com.centyllion.model.ResultPage
import com.centyllion.model.Subscription
import com.centyllion.model.SubscriptionParameters
import com.centyllion.model.SubscriptionState
import com.centyllion.model.User
import com.centyllion.model.UserDetails
import io.ktor.auth.jwt.JWTPrincipal
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils

/**
 * Handles the subscription life cycle.
 *
 * When created a [Subscription] is `waiting` for validation.
 * Upon validation it can be `engaged` or `refused` on failure.
 * When expired a subscription becomes `disengaged` and procedures for auto renewal can be done if needed.
 */
class SubscriptionManager(val config: ServerConfig) {

    fun getAllUsers(detailed: Boolean, offset: Int = 0, limit: Int = 20): ResultPage<User> =
        config.data.getAllUsers(detailed, offset, limit).let { it.copy(content = it.content.map { u -> checkUser(u) }) }

    fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User =
        config.data.getOrCreateUserFromPrincipal(principal).also { checkUser(it) }

    fun getUser(id: String, detailed: Boolean): User? =
        config.data.getUser(id, detailed)?.also { checkUser(it) }


    fun create(userId: String, parameters: SubscriptionParameters) =
        config.data.createSubscription(userId, false, parameters)

    fun validate(subscription: Subscription, user: User, accepted: Boolean): Subscription {
        val details = user.details ?: return subscription

        // updates user subscription
        val type = subscription.subscription
        if (accepted) {
            config.authorization.joinGroup(details.keycloakId, type.groupId)
            config.data.saveUser(user.copy(details = details.copy(subscription = type)))
        } else {
            config.authorization.leaveGroup(details.keycloakId, type.groupId)
            config.data.saveUser(user.copy(details = details.copy(subscription = SubscriptionType.Apprentice)))
        }

        // updates subscription
        val state = if (accepted) SubscriptionState.Engaged else SubscriptionState.Refused
        val updated = subscription.copy(state = state)
        config.data.saveSubscription(updated)

        return updated
    }

    fun expire(user: User, details: UserDetails, subscription: Subscription): Triple<User, UserDetails, Subscription> {
        // updates user subscription
        val type = subscription.subscription
        config.authorization.leaveGroup(details.keycloakId, type.groupId)
        val newDetails = details.copy(subscription = SubscriptionType.Apprentice)
        val newUser = user.copy(details = newDetails)
        config.data.saveUser(newUser)

        // updates subscription
        val newSubscription = subscription.copy(state = SubscriptionState.Disengaged)
        config.data.saveSubscription(newSubscription)

        return Triple(newUser, newDetails, newSubscription)
    }

    fun checkUser(user: User): User {
        val details = user.details ?: return user
        val now = DateTimeUtils.currentTimeMillis()
        // expires all Engaged and non active subscriptions
        val date = if (details.subscriptionUpdatedOn != null) DateTime(details.subscriptionUpdatedOn) else null
        return when {
            // updates at most one time a day
            date == null || date.plusDays(1).isBeforeNow -> {
                config.data.subscriptionsForUser(user.id)
                    .filter { it.state == SubscriptionState.Engaged && !it.active(now) }
                    .fold(user to details) { previous, it ->
                        val result = expire(previous.first, previous.second, it)
                        result.first to result.second
                    }.first
            }
            else -> user
        }
    }
}

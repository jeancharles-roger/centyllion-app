package com.centyllion.model

import com.centyllion.common.SubscriptionType
import kotlinx.serialization.Serializable

val emptyUser = User("", "", "", null)

fun <T> emptyResultPage() = ResultPage<T>(emptyList(), 0, 0)

@Serializable
data class ResultPage<T>(
    val content: List<T>,
    val offset: Int,
    val totalSize: Int
)

@Serializable
data class User(
    val id: String,
    val name: String,
    val username: String,
    val details: UserDetails? = null
)

@Serializable
data class UserDetails(
    val keycloakId: String,
    val email: String,
    val stripeId: String?,
    val subscription: SubscriptionType,
    val subscriptionUpdatedOn: Long?
)

@Serializable
data class SubscriptionParameters(
    val autoRenew: Boolean,
    val subscription: SubscriptionType,
    val duration: Int,
    val amount: Double,
    val paymentMethod: String
)

enum class SubscriptionState {
    Waiting, Refused, Engaged, Disengaged
}

@Serializable
data class Subscription(
    val id: String,

    val userId: String,
    val sandbox: Boolean,
    val autoRenew: Boolean,
    val cancelled: Boolean,

    val startedOn: Long,
    val payedOn: Long?,
    val expiresOn: Long?,
    val cancelledOn: Long?,

    val subscription: SubscriptionType,
    val duration: Int,
    val amount: Double,
    val paymentMethod: String,

    val state: SubscriptionState = SubscriptionState.Waiting
) {

    fun active(now: Long) = !cancelled && now >= startedOn && (expiresOn == null || now <= expiresOn)

    fun parameters() = SubscriptionParameters(autoRenew, subscription, duration, amount, paymentMethod)

}

class Asset(
    val id: String,
    val name: String,
    val data: ByteArray
)

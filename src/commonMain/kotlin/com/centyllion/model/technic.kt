package com.centyllion.model

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
    val roles: List<String>
)

@Serializable
data class Subscription(
    val id: String,

    val userId: String,
    val sandbox: Boolean,
    val autoRenew: Boolean,
    val cancelled: Boolean,

    val startedOn: Long,
    val payedOn: Long?,
    val expiresOn: Long,
    val cancelledOn: Long?,

    val subscription: String,
    val duration: Int,
    val amount: Double,
    val paymentMethod: String
) {

    fun active(now: Long) = !cancelled && now >= startedOn && now <= expiresOn

}

class Asset(
    val id: String,
    val name: String,
    val data: ByteArray
)

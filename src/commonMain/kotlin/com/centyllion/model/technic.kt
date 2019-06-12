package com.centyllion.model

import kotlinx.serialization.Serializable

val emptyUser = User("", "", null)

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
    val details: UserDetails? = null
)

@Serializable
data class UserDetails(
    val keycloakId: String,
    val email: String,
    val stripeId: String?,
    val roles: List<String>
)

class Asset(
    val id: String,
    val name: String,
    val data: ByteArray
)

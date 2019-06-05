package com.centyllion.model

import kotlinx.serialization.Serializable

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

enum class Action {
    Create, Save, Delete, Error
}

@Serializable
data class Event(
    val id: String,
    val createOn: String,
    val userId: String,
    val action: Action,
    val targetId: String,
    val argument: String
)

class Asset(
    val id: String,
    val name: String,
    val data: ByteArray
)

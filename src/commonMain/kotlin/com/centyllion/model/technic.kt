package com.centyllion.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val keycloakId: String,
    val name: String,
    val email: String
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

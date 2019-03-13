package com.centyllion.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val _id: String,
    val keycloakId: String,
    val name: String,
    val email: String
)

enum class EventType {
    CreateUser, SaveUser
}

@Serializable
data class Event(
    val _id: String,
    val type: EventType,
    val arguments: List<String>,
    val date: String

)

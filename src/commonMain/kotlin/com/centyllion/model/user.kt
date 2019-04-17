package com.centyllion.model

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val _id: String,
    @Optional val version: Int = version(serializer()),
    val keycloakId: String,
    val name: String,
    val email: String
)

enum class Action {
    Create, Save, Delete, Error
}

@Serializable
data class Event(
    val _id: String,
    @Optional val version: Int = version(serializer()),
    val date: String,
    val userId: String,
    val action: Action,
    val collection: String,
    val arguments: List<String>
)

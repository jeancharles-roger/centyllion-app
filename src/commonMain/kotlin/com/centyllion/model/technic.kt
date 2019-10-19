package com.centyllion.model

import kotlinx.serialization.Serializable

val emptyUser = User("", "", "", null)

fun <T> emptyResultPage() = ResultPage<T>(emptyList(), 0, 0)

@Serializable
data class Info(
    val app: String,
    val debug: Boolean,
    val dry: Boolean
)

interface Ided {
    val id: String
}

data class Problem(
    val source: ModelElement,
    val message: String
)

@Serializable
data class ResultPage<T>(
    val content: List<T>,
    val offset: Int,
    val totalSize: Int
)

@Serializable
data class User (
    override val id: String,
    val name: String,
    val username: String,
    val details: UserDetails? = null
): Ided

@Serializable
data class UserDetails(
    val keycloakId: String,
    val email: String,
    val tutorialDone: Boolean = false
)

@Serializable
data class UserOptions(
    val tutorialDone: Boolean = false
)

@Serializable
class Asset(
    override val id: String,
    val name: String,
    val entries: List<String>,
    val userId: String
): Ided

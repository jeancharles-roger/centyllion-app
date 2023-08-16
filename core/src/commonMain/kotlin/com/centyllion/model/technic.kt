package com.centyllion.model

import kotlinx.serialization.Serializable

val emptyUser = User("", "", "")

fun <T> emptyResultPage() = ResultPage<T>(emptyList(), 0, 0)

@Serializable
data class CollectionInfo(
    val total: Long,
    val lastWeek: Long,
    val lastMonth: Long
)

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
    val property: String,
    val message: String
)

@Serializable
data class ResultPage<T>(
    val content: List<T>,
    val offset: Long,
    val totalSize: Long
)

@Serializable
data class User (
    override val id: String,
    val name: String,
    val username: String,
): Ided

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

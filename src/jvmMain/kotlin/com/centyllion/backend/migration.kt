package com.centyllion.backend

import com.centyllion.model.GrainModelDescription
import kotlinx.serialization.KSerializer
import org.bson.Document

abstract class Migration(val from: Int, val to: Int) {
    abstract fun migrate(document: Document)
}


val migrationGrainModelV0toV1 = object : Migration(0, 1) {
    override fun migrate(document: Document) {
        document.get("")
    }
}

val migrations: Map<KSerializer<*>, List<Migration>> = mapOf(
    GrainModelDescription.serializer() to listOf()
)

val migrationVersions = migrations
    .map { it.key to (it.value.map { it.to }.max() ?: 0) }.toMap()

fun <T> migrate(serializer: KSerializer<T>, document: Document): Document {
    val targetVersion = migrationVersions[serializer] ?: 0
    val currentVersion = document.getInteger("version") ?: 0
    if (currentVersion < targetVersion) {

    }
    return document
}

package com.centyllion.backend

import com.centyllion.model.GrainModelDescription
import kotlinx.serialization.KSerializer
import org.bson.Document

abstract class Migration(val from: Int, val to: Int) {
    abstract fun migrate(document: Document)
}

val migrationGrainModelV0toV1 = object : Migration(0, 1) {
    override fun migrate(document: Document) {
        val model = document["model"]
        if (model is Document) {
            val behaviours = model["behaviours"]
            if (behaviours is List<*>) {
                behaviours.filterIsInstance<Document>().forEach { behaviour ->
                    val transform = behaviour.getBoolean("transform") ?: false
                    behaviour.remove("transform")
                    behaviour["sourceReactive"] = if (transform) 0 else -1

                    val reactions = behaviour["reaction"]
                    if (reactions is List<*>) {
                        reactions.filterIsInstance<Document>().forEach { reaction ->
                            val reactionTransform = reaction.getBoolean("transform") ?: false
                            reaction.remove("transform")
                            reaction["sourceReactive"] = if (reactionTransform) 0 else -1
                        }
                    }
                }
            }
        }
    }
}

val migrations: Map<KSerializer<*>, List<Migration>> = mapOf(
    GrainModelDescription.serializer() to listOf(migrationGrainModelV0toV1)
)

/** Migrates document to current version using the [migrations]. */
fun <T> migrate(serializer: KSerializer<T>, document: Document): Document {
    val currentVersion = document.getInteger("version") ?: 0
    (migrations[serializer] ?: emptyList()).filter { it.from >= currentVersion }.forEach {
        it.migrate(document)
        document["version"] = it.to
    }
    return document
}
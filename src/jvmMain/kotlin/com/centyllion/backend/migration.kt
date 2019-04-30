package com.centyllion.backend

import com.centyllion.model.GrainModelDescription
import com.centyllion.model.version
import com.centyllion.model.versions
import kotlinx.serialization.KSerializer
import org.bson.Document

abstract class Migration(val from: Int, val to: Int) {
    abstract fun migrate(document: Document)
}

// Moves from transform to sourceReactive
val migrationGrainModelDescriptionV0toV1 = object : Migration(0, 1) {
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

// Moves from figure to icon and size
val migrationGrainModelDescriptionV1toV2 = object : Migration(1, 2) {
    override fun migrate(document: Document) {
        val model = document["model"]
        if (model is Document) {
            val grains = model["grains"]
            if (grains is List<*>) {
                grains.filterIsInstance<Document>().forEach { grain ->
                    grain.remove("figure")
                    grain["icon"] = "square"
                    grain["size"] = 1.0
                }
            }
        }
    }
}

val migrations: Map<KSerializer<*>, List<Migration>> = mapOf(
    GrainModelDescription.serializer() to listOf(
        migrationGrainModelDescriptionV0toV1, migrationGrainModelDescriptionV1toV2
    )
)

fun latestVersion(serializer: KSerializer<*>) =
    migrations.getOrElse(serializer) { emptyList() }.map { it.to }.max() ?: 0

fun checkVersionsAndMigrations() {
    (versions + migrations).map {
        val migrationVersion = latestVersion(it.key)
        val modelVersion = version(it.key)
        if (migrationVersion != modelVersion) {
            throw Exception("Migration version $migrationVersion is not aligned to model $modelVersion for ${it.key.descriptor.name}")
        }
    }
}


/** Migrates document to current version using the [migrations]. */
fun <T> migrate(serializer: KSerializer<T>, document: Document): Document {
    val currentVersion = document.getInteger("version") ?: 0
    (migrations[serializer] ?: emptyList()).filter { it.from >= currentVersion }.forEach {
        it.migrate(document)
        document["version"] = it.to
    }
    return document
}

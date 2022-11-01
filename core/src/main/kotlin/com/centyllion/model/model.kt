package com.centyllion.model

import kotlinx.serialization.Serializable

interface ModelElement {
    val uuid: String
    val name: String
    val description: String
}

fun <T> List<T>.identityFirstIndexOf(value: T): Int {
    val identity = this.indexOfFirst { it === value }
    return if (identity < 0) this.indexOf(value) else identity
}

// TODO removes Asset3d
@Serializable
data class Asset3d(
    val url: String, val opacity: Double = 1.0,
    val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0,
    val xScale: Double = 1.0, val yScale: Double = 1.0, val zScale: Double = 1.0,
    val xRotation: Double = 0.0, val yRotation: Double = 0.0, val zRotation: Double = 0.0
)

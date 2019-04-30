package com.centyllion.model

import kotlin.random.Random

fun dendriteModel(): GrainModel {
    val ms = Grain(0, "ms", "blue", description = "Manganèse soluble")
    val mc = Grain(1, "mc", "red", description = "Manganèse cristallisé", movementProbability = 0.0)

    val r1 = Behaviour(
        "cristalisation", "Cristalisation du Manganèse", 1.0,
        mainReactiveId = mc.id, mainProductId = mc.id, reaction = listOf(Reaction(ms.id, mc.id))
    )

    return GrainModel("Dendrite", "test model", listOf(ms, mc), listOf(r1))
}

fun dendriteSimulation(width: Int = 100, height: Int = 100): Simulation {
    val size = width * height
    val agents = (0 until size).map {
        when (it) {
            size/2 -> 1
            else -> if (Random.nextDouble() < 0.15) 0 else -1
        }
    }
    return Simulation("", "", width, height, 1, agents)
}

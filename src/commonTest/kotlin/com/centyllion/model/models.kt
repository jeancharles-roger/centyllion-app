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

fun bacteriaModel(): GrainModel {
    val bacteria = Grain(0, "b", "red", Figure.Square, "Bacteria")
    val sugar = Grain(1, "s", "blue", Figure.Square, "Sugar", allowedDirection = emptySet())

    val division = Behaviour(
        "Division", "Bacteria division", 1.0,
        mainReactiveId = 0 , mainProductId = 0, reaction = listOf(Reaction(1, 0))
    )
    return GrainModel("Bacteria", "Bacteria model", listOf(bacteria, sugar), listOf(division))
}

fun bacteriaSimulation(width: Int = 100, height: Int = 100): Simulation {
    val size = width * height
    val agents = (0 until size).map {
        when (it) {
            size/2 -> 1
            else -> if (Random.nextDouble() < 0.15) 0 else -1
        }
    }
    return Simulation("", "", width, height, 1, agents)
}

fun immunityModel(): GrainModel {
    val si = Grain(0, "si", "lightgreen", Figure.Square, "Immunity system", 50)
    val bacteria = Grain(1, "b", "red", Figure.Square, "Bacteria")
    val boneMarrow = Grain(2, "bm", "blue", Figure.Square, "Bone marrow", movementProbability = 0.0)

    val division = Behaviour(
        "Division", "Bacteria division", 0.01,
        mainReactiveId = 1, mainProductId = 1,
        reaction = listOf(Reaction(-1, 1))
    )
    val defense = Behaviour(
        "Defense", "System defense", 1.0,
        mainReactiveId = 0, mainProductId = 0,
        reaction = listOf(Reaction(1, 0))
    )
    val siProduction = Behaviour(
        "Production", "Bone marrow si production", 0.1,
        mainReactiveId = 2, mainProductId = 2,
        reaction = listOf(Reaction(-1, 0))
    )

    return GrainModel(
        "Immunity system", "Immunity system model with a bacteria to test it",
        listOf(si, bacteria, boneMarrow),
        listOf(division, defense, siProduction)
    )
}

fun immunitySimulation(width: Int = 100, height: Int = 100): Simulation {
    val size = width * height
    val agents = (0 until size).map {
        val p = Random.nextDouble()
        when {
            it == size/2 - 5 || it == size/2 + 5 -> 2
            p < 0.001 -> 0
            p < 0.002 -> 1
            else -> -1
        }
    }
    return Simulation("", "", width, height, 1, agents)
}

fun fishRespirationModel(co: Boolean): GrainModel {
    val branchiaSide = Grain(0, "pb", "darkblue", description = "Paroi branchie", movementProbability = 0.0)
    val waterO2 = Grain(1, "oe", "lightblue", description = "Oxygène dans l'eau",
        allowedDirection = setOf(Direction.Up, Direction.Down, Direction.Right))
    val waterO2Source = Grain(2, "soe", "plum", description = "Source d'oxygène dans l'eau", movementProbability = 0.0)
    val waterO2Sink = Grain(3, "eoe", "violet", description = "Evacuation d'oxygène dans l'eau", movementProbability = 0.0)

    val waterO2SourceBehaviour = Behaviour("Apport d'oxygene", "", 0.1,
        mainReactiveId = waterO2Source.id, mainProductId = waterO2Source.id,
        reaction = listOf(Reaction(-1,  waterO2.id, allowedDirection = setOf(Direction.Right)))
    )
    val waterO2SinkBehaviour = Behaviour("Evacuation d'oxygene", "", 1.0,
        mainReactiveId = waterO2Sink.id, mainProductId = waterO2Sink.id,
        reaction = listOf(Reaction(waterO2.id,  -1, allowedDirection = setOf(Direction.Left)))
    )

    val veinSide = Grain(4, "px", "firebrick", description = "Paroi veine", movementProbability = 0.0)
    val bloodO2 = Grain(5, "os", "red", description = "Oxygène dans le sang",
        allowedDirection = setOf(Direction.Up, Direction.Down, if (co) Direction.Right else Direction.Left))

    val waterToBlood = Behaviour("Passage eau vers sang", "", 0.2,
        mainReactiveId = branchiaSide.id, mainProductId = branchiaSide.id,
        reaction = listOf(
            Reaction(waterO2.id,  -1, allowedDirection = setOf(Direction.Down)),
            Reaction(-1,  bloodO2.id, allowedDirection = setOf(Direction.Up))
        )
    )

    val bloodToWater = Behaviour("Passage sang vers eau", "", 0.2,
        mainReactiveId = branchiaSide.id, mainProductId = branchiaSide.id,
        reaction = listOf(
            Reaction( bloodO2.id,  -1, allowedDirection = setOf(Direction.Up)),
            Reaction(-1,  waterO2.id, allowedDirection = setOf(Direction.Down))
        )
    )

    return GrainModel("Respiration des poissons (${if (co) "Co" else "Contre"})", "",
        listOf(branchiaSide, waterO2, waterO2Source, waterO2Sink, veinSide, bloodO2),
        listOf(waterO2SourceBehaviour, waterO2SinkBehaviour, waterToBlood, bloodToWater)
    )
}

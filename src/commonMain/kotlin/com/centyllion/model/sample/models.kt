package com.centyllion.model.sample

import com.centyllion.model.*
import kotlin.random.Random

val emptyModel = GrainModel("empty")
val emptyGrainModelDescription = GrainModelDescription("", "", null, null, "", emptyModel)
val emptySimulation = Simulation(100, 100, 1)

fun dendriteModel(): GrainModel {
    val ms = Grain(0, "ms", "blue", description = "Manganèse soluble")
    val mc = Grain(1, "mc", "red", description = "Manganèse cristallisé", movementProbability = 0.0)

    val r1 = Behaviour(
        "cristalisation", "Cristalisation du Manganèse", 1.0,
        mainReaction = Reaction(mc.id, mc.id), reaction = listOf(Reaction(ms.id, mc.id))
    )

    return GrainModel("Dendrite", "test model", listOf(ms, mc), listOf(r1))
}

fun dendriteSimulation(width: Int = 100, height: Int = 100): Simulation {
    return Simulation(width, height, 1).apply {
        for (i in 0 until dataSize) {
            val p = Random.nextDouble()
            when {
                p < 0.15 -> setIdAtIndex(i, 0)
            }
        }

        for (i in -2 until 2) {
            setIdAtIndex(dataSize / 2 + i * 167, 1)
        }

        saveState()
    }
}


fun carModel(): GrainModel {
    val road = Grain(0, "road", "#DDDDDD", description = "Road", movementProbability = 0.0)
    val carFront = Grain(1, "f", "blue", description = "Car front", movementProbability = 0.0)
    val carBack = Grain(2, "b", "red", description = "Car back", movementProbability = 0.0)

    val move = Behaviour(
        "Move", "Move car", 1.0,
        mainReaction = Reaction(carFront.id, carBack.id),
        reaction = listOf(Reaction(carBack.id, road.id), Reaction(road.id, carFront.id))
    )
    val reverse = Behaviour(
        "Reverse", "Reverse car", 1.0,
        mainReaction = Reaction(carFront.id, carBack.id),
        reaction = listOf(Reaction(carFront.id, carFront.id), Reaction(carBack.id, carFront.id))
    )
    return GrainModel("Cars", "On the road again", listOf(road, carFront, carBack), listOf(move, reverse))
}

fun carSimulation(width: Int = 100, height: Int = 100, insideLines: Int = 4) =
    Simulation(width, height, 1).apply {
        for (i in 1 until width - 1) {
            for (j in 1 until height - 1) {
                if (i == 1 || j == 1 || i == width - 2 || j == height - 2) {
                    setIdAtIndex(toIndex(i, j), 0)
                }

                for (k in 0..insideLines) {
                    if (i < width - 2 && j == (k * height / (insideLines + 1))) {
                        when (i) {
                            1 -> {
                                setIdAtIndex(toIndex(i, j), 2)
                            }
                            2 -> {
                                setIdAtIndex(toIndex(i, j), 1)
                            }
                            else -> {
                                setIdAtIndex(toIndex(i, j), 0)
                            }
                        }

                    }
                    if (j < height - 2 && i == (k * width / (insideLines + 1))) {
                        when (j) {
                            1 -> {
                                setIdAtIndex(toIndex(i, j), 2)
                            }
                            2 -> {
                                setIdAtIndex(toIndex(i, j), 1)
                            }
                            else -> {
                                setIdAtIndex(toIndex(i, j), 0)
                            }
                        }
                    }
                }
            }
        }
        saveState()
    }

fun bacteriaModel(): GrainModel {
    val bacteria = Grain(0, "b", "red", Figure.Square, "Bacteria")
    val sugar = Grain(1, "s", "blue", Figure.Square, "Sugar", allowedDirection = emptySet())

    val division = Behaviour(
        "Division", "Bacteria division", 1.0,
        mainReaction = Reaction(0, 0), reaction = listOf(Reaction(1, 0))
    )
    return GrainModel("Bacteria", "Bacteria model", listOf(bacteria, sugar), listOf(division))
}

fun bacteriaSimulation(width: Int = 100, height: Int = 100): Simulation {
    return Simulation(width, height, 1).apply {
        for (i in 0 until dataSize) {
            val p = Random.nextDouble()
            when {
                p < 0.15 -> setIdAtIndex(i, 1)
            }
        }

        setIdAtIndex(dataSize / 4 + width / 2, 0)

        saveState()
    }
}

fun immunityModel(): GrainModel {
    val si = Grain(0, "si", "lightgreen", Figure.Square, "Immunity system", 50)
    val bacteria = Grain(1, "b", "red", Figure.Square, "Bacteria")
    val boneMarrow = Grain(2, "bm", "blue", Figure.Square, "Bone marrow", movementProbability = 0.0)

    val division = Behaviour(
        "Division", "Bacteria division", 0.01,
        mainReaction = Reaction(1, 1), reaction = listOf(Reaction(-1, 1))
    )
    val defense = Behaviour(
        "Defense", "System defense", 1.0,
        mainReaction = Reaction(0, 0), reaction = listOf(Reaction(1, 0))
    )
    val siProduction = Behaviour(
        "Production", "Bone marrow si production", 0.1,
        mainReaction = Reaction(2, 2), reaction = listOf(Reaction(-1, 0))
    )

    return GrainModel(
        "Immunity system", "Immunity system model with a bacteria to test it",
        listOf(si, bacteria, boneMarrow),
        listOf(division, defense, siProduction)
    )
}

fun immunitySimulation(width: Int = 100, height: Int = 100): Simulation {
    return Simulation(width, height, 1).apply {
        for (i in 0 until dataSize) {
            val p = Random.nextDouble()
            when {
                p < 0.001 -> setIdAtIndex(i, 0)
                p < 0.002 -> setIdAtIndex(i, 1)
            }
        }

        setIdAtIndex(toIndex(width / 2 - 5, height / 2 - 5), 2)
        setIdAtIndex(toIndex(width / 2 + 5, height / 2 + 5), 2)

        saveState()
    }
}

fun fishRespirationModel(co: Boolean): GrainModel {
    val branchiaSide = Grain(0, "Paroi branchie", "darkblue", movementProbability = 0.0)
    val waterO2 = Grain(1, "Oxygène dans l'eau", "lightblue",
        allowedDirection = setOf(Direction.Up, Direction.Down, Direction.Right))
    val waterO2Source = Grain(2, "Source d'oxygène dans l'eau", "plum", movementProbability = 0.0)
    val waterO2Sink = Grain(3, "Evacuation d'oxygène dans l'eau", "violet", movementProbability = 0.0)

    val waterO2SourceBehaviour = Behaviour("Apport d'oxygene", "", 0.1,
        mainReaction = Reaction(waterO2Source.id, waterO2Source.id),
        reaction = listOf(Reaction(-1,  waterO2.id, allowedDirection = setOf(Direction.Right)))
    )
    val waterO2SinkBehaviour = Behaviour("Evacuation d'oxygene", "", 1.0,
        mainReaction = Reaction(waterO2Sink.id, waterO2Sink.id),
        reaction = listOf(Reaction(waterO2.id,  -1, allowedDirection = setOf(Direction.Left)))
    )

    val veinSide = Grain(4, "Paroi veine", "firebrick", movementProbability = 0.0)
    val bloodO2 = Grain(5, "Oxygène dans le sang", "red",
        allowedDirection = setOf(Direction.Up, Direction.Down, if (co) Direction.Right else Direction.Left))

    val waterToBlood = Behaviour("Passage eau vers sang", "", 0.2,
        mainReaction = Reaction(branchiaSide.id, branchiaSide.id),
        reaction = listOf(
            Reaction(waterO2.id,  -1, allowedDirection = setOf(Direction.Down)),
            Reaction(-1,  bloodO2.id, allowedDirection = setOf(Direction.Up))
        )
    )

    val bloodToWater = Behaviour("Passage sang vers eau", "", 0.2,
        mainReaction = Reaction(branchiaSide.id, branchiaSide.id),
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


fun fishRespirationSimulation(): Simulation {
    return Simulation(100, 100, 1).apply {
        for (i in 0 until width) {
            // main branchia
            setIdAtIndex(toIndex(i, 45), 0)
            setIdAtIndex(toIndex(i, 55), 0)

            // main vein
            setIdAtIndex(toIndex(i, if (i in 20..80) 35 else 0), 4)
            if (i < 30 || i > 70) setIdAtIndex(toIndex(i, 44), 4)
        }

        // vein sinks
        for (i in 0 until 45) {
            setIdAtIndex(toIndex(0, i), 4)
            if (i < 35) {
                setIdAtIndex(toIndex(20, i), 4)
                setIdAtIndex(toIndex(80, i), 4)
            }
            setIdAtIndex(toIndex(99, i), 4)
        }

        // water o2 source and sinks
        for (i in 0 until 9) {
            setIdAtIndex(toIndex(0, 46+i), 2)
            setIdAtIndex(toIndex(99, 46+i), 3)
        }

        saveState()
    }
}

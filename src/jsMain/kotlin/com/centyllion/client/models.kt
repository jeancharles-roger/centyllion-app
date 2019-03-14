package com.centyllion.client

import com.centyllion.model.*
import kotlin.random.Random

fun emptyModelAndSimulation(width: Int = 100, height: Int = 100): Simulation {
    val model = GrainModel("empty")
    return Simulation(width, height, 1, model)
}

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
    return Simulation(width, height, 1, dendriteModel()).apply {
        for (i in 0 until dataSize) {
            val p = Random.nextDouble()
            when {
                p < 0.15 -> addGrainAtIndex(i, this.model.grains[0])
            }
        }

        for (i in -2 until 2) {
            addGrainAtIndex(dataSize / 2 + i * 167, this.model.grains[1])
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
    Simulation(width, height, 1, carModel()).apply {
        for (i in 1 until width - 1) {
            for (j in 1 until height - 1) {
                if (i == 1 || j == 1 || i == width - 2 || j == height - 2) {
                    addGrainAtIndex(toIndex(Position(i, j, 0)), model.grains[0])
                }

                for (k in 0..insideLines) {
                    if (i < width - 2 && j == (k * height / (insideLines + 1))) {
                        when (i) {
                            1 -> addGrainAtIndex(toIndex(Position(i, j, 0)), model.grains[2])
                            2 -> addGrainAtIndex(toIndex(Position(i, j, 0)), model.grains[1])
                            else -> addGrainAtIndex(toIndex(Position(i, j, 0)), model.grains[0])
                        }

                    }
                    if (j < height - 2 && i == (k * width / (insideLines + 1))) {
                        when (j) {
                            1 -> addGrainAtIndex(toIndex(Position(i, j, 0)), model.grains[2])
                            2 -> addGrainAtIndex(toIndex(Position(i, j, 0)), model.grains[1])
                            else -> addGrainAtIndex(toIndex(Position(i, j, 0)), model.grains[0])
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
    return GrainModel("Bacteria", "", listOf(bacteria, sugar), listOf(division))
}

fun bacteriaSimulation(width: Int = 100, height: Int = 100): Simulation {
    return Simulation(width, height, 1, bacteriaModel()).apply {
        for (i in 0 until dataSize) {
            val p = Random.nextDouble()
            when {
                p < 0.15 -> addGrainAtIndex(i, this.model.grains[1])
            }
        }

        addGrainAtIndex(dataSize / 4 + width / 2, this.model.grains[0])

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
        "Immunity system", "",
        listOf(si, bacteria, boneMarrow),
        listOf(division, defense, siProduction)
    )
}

fun immunitySimulation(width: Int = 100, height: Int = 100): Simulation {
    return Simulation(width, height, 1, immunityModel()).apply {
        for (i in 0 until dataSize) {
            val p = Random.nextDouble()
            when {
                p < 0.001 -> addGrainAtIndex(i, this.model.grains[0])
                p < 0.002 -> addGrainAtIndex(i, this.model.grains[1])
            }
        }

        addGrainAtIndex(toIndex(Position(width / 2 - 5, height / 2 - 5, 0)), this.model.grains[2])
        addGrainAtIndex(toIndex(Position(width / 2 + 5, height / 2 + 5, 0)), this.model.grains[2])

        saveState()
    }
}

package com.centyllion.client

import com.centyllion.common.*
import kotlin.random.Random

fun emptyModelAndSimulation(width: Int = 100, height: Int = 100): Simulation {
    val model = Model("empty", width, height)
    return Simulation(model)
}

fun dendriteModel(width: Int = 100, height: Int = 100): Model {
    val ms = Grain(0, "ms", "blue", description = "Manganèse soluble")
    val mc = Grain(1, "mc", "red", description = "Manganèse cristallisé", movementProbability = 0.0)

    val r1 = Behaviour(
        "cristalisation", "Cristalisation du Manganèse", 1.0,
        mainReaction = Reaction(mc.id, mc.id), reaction = listOf(Reaction(ms.id, mc.id))
    )

    return Model(
        "m1", width, height, 1, "test model",
        listOf(ms, mc), listOf(r1)
    )
}

fun dendriteSimulation(width: Int = 100, height: Int = 100): Simulation {
    return Simulation(dendriteModel(width, height)).apply {
        for (i in 0 until model.dataSize) {
            val p = Random.nextDouble()
            when {
                p < 0.15 -> addGrainAtIndex(i, this.model.grains[0])
            }
        }

        for (i in -2 until 2) {
            addGrainAtIndex(model.dataSize / 2 + i * 167, this.model.grains[1])
        }
    }
}


fun carModel(width: Int = 100, height: Int = 100): Model {
    val road = Grain(0, "road", "#DDDDDD", description = "Road", movementProbability = 0.0)
    val carFront = Grain(1, "f", "blue", description = "Car front", movementProbability = 0.0)
    val carBack = Grain(2, "b", "red", description = "Car back", movementProbability = 0.0)

    val behaviour = Behaviour(
        "Move", "move car", 1.0,
        mainReaction = Reaction(carFront.id, carBack.id),
        reaction = listOf(Reaction(carBack.id, road.id), Reaction(road.id, carFront.id))
    )
    return Model("cars", width, height, 1, "On the road again", listOf(road, carFront, carBack), listOf(behaviour))
}

fun carSimulation(width: Int = 100, height: Int = 100, insideLines: Int = 4) = Simulation(carModel(width, height)).apply {
    for (i in 1 until width - 1) {
        for (j in 1 until height - 1) {
            if (i == 1 || j == 1 || i == width - 2 || j == height - 2) {
                addGrainAtIndex(model.toIndex(Position(i, j, 0)), model.grains[0])
            }

            for (k in 0..insideLines) {
                if (i < width - 2 && j == (k * height / (insideLines+1))) {
                    addGrainAtIndex(model.toIndex(Position(i, j, 0)), model.grains[0])
                }
                if (j < height - 2 && i == (k * width / (insideLines+1))) {
                    addGrainAtIndex(model.toIndex(Position(i, j, 0)), model.grains[0])
                }
            }
        }
    }

    addGrainAtIndex(model.toIndex(Position(width - 3, height / 2, 0)), model.grains[1])
    addGrainAtIndex(model.toIndex(Position(width - 4, height / 2, 0)), model.grains[2])

}

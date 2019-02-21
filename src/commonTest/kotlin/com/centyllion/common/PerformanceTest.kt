package com.centyllion.common

import kotlin.random.Random
import kotlin.test.Test

class PerformanceTest {

    fun dendriteModel(width: Int = 100, height: Int = 100): Model {
        val ms = Grain(0, "ms", "blue")
        val mc = Grain(1, "mc", "red", movementProbability = 0.0)

        val r1 = Behaviour(
            "crystalisation", "", 1.0,
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
                addGrainAtIndex(model.dataSize/2 + i*167, this.model.grains[1])
            }
        }
    }


    @Test
    fun testDendritePerformance() {
        val simulator = Simulator(dendriteSimulation(200, 200))
        repeat(500) { simulator.oneStep() }
    }

}

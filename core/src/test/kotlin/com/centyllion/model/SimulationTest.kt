package com.centyllion.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SimulationTest {

    @Test
    fun testBugSimpleOne2OneTransfo() {
        // creates the simulations
        val one2oneModel = GrainModel(
            "one2one", "Test one 2 one behaviour",
            grains = listOf(Grain(0, "Source", movementProbability = 0.0), Grain(1, "Target")),
            behaviours = listOf(Behaviour("one2one", mainReactiveId = 0, mainProductId = 1))
        )

        val agents = List(100*100) { if (it % 20 == 0) 0 else -1}
        val one2oneSimulation = createSimulation("Test one 2 one", agents = agents)

        val simulator = Simulator(one2oneModel, one2oneSimulation)
        simulator.grainsCounts().let { counts ->
            assertEquals(500, counts[0])
            assertEquals(0, counts[1] ?: 0)
        }

        // runs the simulator
        simulator.oneStep()

        simulator.grainsCounts().let { counts ->
            assertEquals(0, counts[0] ?: 0)
            assertEquals(500, counts[1] ?: 0)
        }
    }

}

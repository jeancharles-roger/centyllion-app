package com.centyllion.common

import kotlin.test.Test
import kotlin.test.assertEquals

class StabilityTest {

    @Test
    fun carTest() {
        val simulation = carSimulation(10, 10)
        val simulator = Simulator(simulation)
        val frontGrain = simulation.model.grains[1]
        val backGrain = simulation.model.grains[2]
        val count = simulator.lastGrainsCount()
        val frontCount = count[frontGrain]
        val backCount = count[backGrain]

        repeat(100) { simulator.oneStep() }
        val lastCount = simulator.lastGrainsCount()
        assertEquals(frontCount, lastCount[frontGrain])
        assertEquals(backCount, lastCount[backGrain])
    }
}

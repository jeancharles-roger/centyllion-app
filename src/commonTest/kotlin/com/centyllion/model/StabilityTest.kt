package com.centyllion.model

import com.centyllion.model.sample.carModel
import com.centyllion.model.sample.carSimulation
import kotlin.test.Test
import kotlin.test.assertEquals

class StabilityTest {

    @Test
    fun carTest() {
        val simulator = Simulator(carModel(), carSimulation(10, 10))
        val count = simulator.lastGrainsCount()

        repeat(100) { simulator.oneStep() }
        assertEquals(count, simulator.lastGrainsCount())
    }
}

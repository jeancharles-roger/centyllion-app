package com.centyllion.model

import com.centyllion.model.sample.dendriteModel
import com.centyllion.model.sample.dendriteSimulation
import kotlin.test.Test

class PerformanceTest {

    @Test
    fun testDendritePerformance() {
        val simulator = Simulator(dendriteModel(), dendriteSimulation(100, 100))
        repeat(500) { simulator.oneStep() }
    }

}

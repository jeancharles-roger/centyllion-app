package com.centyllion.common

import kotlin.test.Test

class PerformanceTest {

    @Test
    fun testDendritePerformance() {
        val simulator = Simulator(dendriteSimulation(200, 200))
        repeat(500) { simulator.oneStep() }
    }

}

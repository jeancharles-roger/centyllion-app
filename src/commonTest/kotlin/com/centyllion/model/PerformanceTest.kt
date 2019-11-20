package com.centyllion.model

import kotlin.test.Test

class PerformanceTest {

    @Test
    fun testDendritePerformance() {
        val simulator = Simulator(dendriteModel(), dendriteSimulation(100, 100))
        repeat(500) { simulator.oneStep() }
    }

    @Test
    fun testAntsPerformance() {
        val simulator = Simulator(antsModel(), antsSimulation(100, 100))
        repeat(300) { simulator.oneStep() }
    }

}

package com.centyllion.model

import kotlin.test.Ignore
import kotlin.test.Test

class PerformanceTest {

    @Test
    fun testDendrite100Performance() {
        val simulator = Simulator(dendriteModel(), dendriteSimulation(100, 100))
        repeat(100) { simulator.oneStep() }
    }
    @Test
    fun testDendrite200Performance() {
        val simulator = Simulator(dendriteModel(), dendriteSimulation(200, 200))
        repeat(100) { simulator.oneStep() }
    }

    @Test @Ignore
    fun testAntsPerformance() {
        val simulator = Simulator(antsModel(), antsSimulation(100, 100))
        repeat(300) { simulator.oneStep() }
    }

}

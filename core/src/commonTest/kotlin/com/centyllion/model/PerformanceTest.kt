package com.centyllion.model

import kotlin.test.Test

class PerformanceTest {

    @Test
    fun testDendritePerformance() {
        val simulator = Simulator(dendriteModel(), dendriteSimulation(100))
        repeat(100) { simulator.oneStep() }
    }

    @Test //@Ignore
    fun testAntsPerformance() {
        val simulator = Simulator(antsModel(), antsSimulation(100))
        repeat(300) { simulator.oneStep() }
    }

    @Test
    fun testFormulaPerformance() {
        val simulator = Simulator(formulaModel(), createSimulation())
        repeat(300) { simulator.oneStep() }
    }

}

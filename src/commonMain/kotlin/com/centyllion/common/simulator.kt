package com.centyllion.common

import kotlin.random.Random

class Simulator(
    val simulation: Simulation,
    val fromInitial: Boolean = false
) {

    init {
        if (fromInitial) {
            simulation.reset()
        }
    }

    val model = simulation.model

    val random = Random.Default

    var step = 0

    fun oneStep() {

        val toExecute = mutableMapOf<Position, Behaviour>()
        val notSelected = mutableSetOf<Position>()
        for (i in 0 until model.dataSize) {
            val grain = model.indexedGrains[simulation.agents[i]]
            if (grain != null) {
                //model.behaviours.filter { it.applicable(grain, 0, neighbours) }
            }
        }


    }
}

package com.centyllion.common

import kotlin.random.Random

data class ApplicableBehavior(
    val index: Int,
    val behaviour: Behaviour,
    val usedNeighbours: List<Direction>
) {

    fun apply(simulation: Simulation) {
        simulation.transform(index, index, behaviour.mainReaction.productId, behaviour.mainReaction.transform)
    }
}

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
        val toExecute = mutableListOf<ApplicableBehavior>()
        val notSelected = mutableSetOf<Pair<Int, Grain>>()
        for (i in 0 until model.dataSize) {
            val grain = simulation.grainAtIndex(i)
            if (grain != null) {
                val age = simulation.ageAtIndex(i)

                // a grain is present, a behaviour can be triggered
                val neighbours = simulation.neighbours(i)

                // searches for applicable behaviours
                val applicable = model.behaviours
                    .filter { it.applicable(grain, age, neighbours) } // found applicable behaviours
                    .filter { random.nextDouble() < it.probability } // filters by probability

                // selects behaviour if any is applicable
                if (applicable.isNotEmpty()) {
                    // selects one at random
                    val behaviour = applicable[random.nextInt(applicable.size)]
                    toExecute.add(ApplicableBehavior(i, behaviour, behaviour.usedAgents(neighbours)))
                } else {
                    // save the grain index as not selected
                    notSelected.add(i to grain)
                }
            }
        }

        // TODO needs to filter behaviors that uses the same grains


        // execute behaviours
        toExecute.forEach { it.apply(simulation) }

        // TODO moves all not-used
        notSelected.forEach {
            val grain = it.second
            if (grain.canMove) {
                // applies random to do move
                if (random.nextDouble() < grain.movementProbability) {
                    val direction = grain.allowedDirection[random.nextInt(grain.allowedDirection.size)]
                    val targetPosition = model.toPosition(it.first).move(direction)
                    val targetIndex = model.toIndex(targetPosition)
                    simulation.transform(it.first, targetIndex, grain.id, true)
                }
            }
        }

        // TODO updates age by one for all

        // TODO apply dying process

        // count a step
        step += 1

    }
}

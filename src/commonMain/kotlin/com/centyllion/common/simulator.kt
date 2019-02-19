package com.centyllion.common

import kotlin.random.Random

data class ApplicableBehavior(
    val index: Int,
    val behaviour: Behaviour,
    val usedNeighbours: List<Pair<Int, Grain>>
) {

    fun apply(simulation: Simulation) {
        // applies main reaction
        simulation.transform(index, index, behaviour.mainReaction.productId, behaviour.mainReaction.transform)

        // applies other reaction find each neighbour for each reaction
        val reactives = usedNeighbours.sortedBy { it.second.id }
        val reactions = behaviour.reaction.sortedBy { it.reactiveId }

        // reactives and reactions must have same size
        for (i in 0 until reactives.size) {
            val reactive = reactives[i]
            val reaction = reactions[i]
            simulation.transform(reactive.first, reactive.first, reaction.productId, reaction.transform)
        }
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
        val all = mutableSetOf<Pair<Int, Grain>>()
        val used = mutableListOf<Pair<Int, Grain>>()
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
                    val usedNeighbours = behaviour.usedAgents(neighbours)
                        .map { simulation.model.moveIndex(i, it).let { it to simulation.grainAtIndex(it)!! } }

                    toExecute.add(ApplicableBehavior(i, behaviour, usedNeighbours))
                    used.add(i to grain)
                    used.addAll(usedNeighbours)
                }
                // save the grain index for move
                all.add(i to grain)
            }
        }

        // TODO needs to filter behaviors that uses the same grains


        // execute behaviours
        toExecute.forEach { it.apply(simulation) }

        (all - used).forEach {
            val index = it.first
            val grain = it.second
            if (grain.canMove) {
                // applies random to do move
                if (random.nextDouble() < grain.movementProbability) {
                    val directions = grain.allowedDirection.filter { simulation.indexIsFree(model.moveIndex(index, it)) }
                    if (directions.isNotEmpty()) {
                        val direction = directions[random.nextInt(directions.size)]
                        val targetIndex = model.moveIndex(index, direction)
                        simulation.transform(index, targetIndex, grain.id, true)
                    }
                }
            }
        }

        // TODO updates age by one for all

        // TODO apply dying process

        // count a step
        step += 1

    }
}

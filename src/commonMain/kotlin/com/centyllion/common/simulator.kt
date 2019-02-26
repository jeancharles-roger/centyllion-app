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
            val sourceIndex = reactive.first
            val reaction = reactions[i]
            simulation.transform(sourceIndex, sourceIndex, reaction.productId, reaction.transform)
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

    private val grainCountHistory = simulation.grainsCounts().let { counts ->
        model.grains.map { it to mutableListOf(counts[it.id] ?: 0) }.toMap()
    }

    var step = 0

    fun lastGrainsCount(): Map<Grain, Int> = grainCountHistory.map { it.key to it.value.last() }.toMap()

    fun oneStep() {

        // applies agents dying process
        val currentCount = mutableMapOf<Grain, Int>()
        for (i in 0 until model.dataSize) {
            val grain = simulation.grainAtIndex(i)
            if (grain != null) {
                // does the grain dies ?
                if (grain.halfLife > 0 && random.nextDouble() < grain.deathProbability) {
                    // it dies, does't count
                    simulation.transform(i, i, null, false)
                } else {
                    currentCount[grain] = currentCount.getOrElse(grain) { 0 } + 1
                    simulation.ageGrain(i)
                }
            }
        }
        // stores count for each grain
        currentCount.forEach {
            grainCountHistory[it.key]?.add(it.value)
        }

        // all will contains index of agents as keys associated to a list of applicable behaviors
        // if an agent doesn't contain any applicable behavior it will have an empty list.
        // if an agent can't move and doesn't contain any applicable behavior, it won't be present in the map
        val all = mutableMapOf<Int, MutableList<ApplicableBehavior>>()
        for (i in 0 until model.dataSize) {
            val grain = simulation.grainAtIndex(i)
            if (grain != null) {
                val selected = all.getOrPut(i) { mutableListOf() }
                if (model.mainReactiveGrains.contains(grain)) {
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
                            .map { simulation.model.moveIndex(i, it).let { index -> index to simulation.grainAtIndex(index)!! } }

                        val behavior = ApplicableBehavior(i, behaviour, usedNeighbours)
                        selected.add(behavior)
                        usedNeighbours.forEach {
                            val neighboursSelected = all.getOrPut(it.first) { mutableListOf() }
                            neighboursSelected.add(behavior)
                        }
                    }
                }
            }
        }

        // filters behaviors that aren't concurrent
        val toExecute = all.filter { it.value.isNotEmpty() }
            .map { it.value[random.nextInt(it.value.size)] }.toSet()
        toExecute.forEach { it.apply(simulation) }

        all.filter { it.value.isEmpty() }.forEach {
            val index = it.key
            val grain = simulation.grainAtIndex(index)
            if (grain != null && grain.canMove) {
                // applies random to do move
                if (random.nextDouble() < grain.movementProbability) {
                    val directions = grain.allowedDirection
                        .filter { model.moveIndex(index, it).let { index -> model.indexInside(index) && simulation.indexIsFree(index) } }
                    if (directions.isNotEmpty()) {
                        val direction = directions[random.nextInt(directions.size)]
                        val targetIndex = model.moveIndex(index, direction)
                        simulation.transform(index, targetIndex, grain.id, true)
                    }
                }
            }
        }

        // count a step
        step += 1
    }
}

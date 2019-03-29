package com.centyllion.model

import kotlin.random.Random

data class ApplicableBehavior(
    val index: Int,
    val behaviour: Behaviour,
    val usedNeighbours: List<Pair<Int, Int>>
) {

    fun apply(simulation: Simulation) {
        // applies main reaction
        simulation.transform(index, index, behaviour.mainProductId, behaviour.transform)

        // applies other reaction find each neighbour for each reaction
        val reactives = usedNeighbours.sortedBy { it.second }
        val reactions = behaviour.reaction.sortedBy { it.reactiveId }

        // applies reactions
        reactions.zip(reactives).forEach { (reaction, reactive) ->
            val sourceIndex = reactive.first
            simulation.transform(sourceIndex, sourceIndex, reaction.productId, reaction.transform)
        }
    }
}

class Simulator(
    val model: GrainModel,
    val simulation: Simulation,
    fromInitial: Boolean = false
) {

    val random = Random.Default

    val grainCountHistory = simulation.grainsCounts().let { counts ->
        model.grains.map { it to mutableListOf(counts[it.id] ?: 0) }.toMap()
    }

    init {
        if (fromInitial) {
            simulation.reset()
        }
    }

    var step = 0

    private val reactiveGrains = model.mainReactiveGrains

    private val allBehaviours = model.allBehaviours

    fun grainAtIndex(index: Int) = model.indexedGrains[simulation.idAtIndex(index)]

    fun lastGrainsCount(): Map<Grain, Int> = grainCountHistory.map { it.key to it.value.last() }.toMap()

    fun oneStep() {

        // applies agents dying process
        val currentCount = mutableMapOf<Grain, Int>()

        // all will contains index of agents as keys associated to a list of applicable behaviors
        // if an agent doesn't contain any applicable behavior it will have an empty list.
        // if an agent can't move and doesn't contain any applicable behavior, it won't be present in the map
        val all = mutableMapOf<Int, MutableList<ApplicableBehavior>>()
        for (i in 0 until simulation.dataSize) {
            val grain = grainAtIndex(i)
            if (grain != null) {

                // does the grain dies ?
                if (grain.halfLife > 0.0 && random.nextDouble() < grain.deathProbability) {
                    // it dies, does't count
                    simulation.transform(i, i, null, false)
                } else {

                    // ages grain
                    currentCount[grain] = currentCount.getOrElse(grain) { 0 } + 1
                    simulation.ageGrain(i)

                    val selected = all.getOrPut(i) { mutableListOf() }
                    if (reactiveGrains.contains(grain)) {
                        val age = simulation.ageAtIndex(i)

                        // a grain is present, a behaviour can be triggered
                        val neighbours = simulation.neighbours(i)

                        // searches for applicable behaviours
                        val applicable = allBehaviours
                            .filter { it.applicable(grain, age, neighbours) } // found applicable behaviours
                            .filter { random.nextDouble() < it.probability } // filters by probability

                        // selects behaviour if any is applicable
                        if (applicable.isNotEmpty()) {
                            // selects one at random
                            val behaviour = applicable[random.nextInt(applicable.size)]

                            // for each reactions, find all possible reactives
                            val possibleReactions = behaviour.reaction
                                .map { reaction ->
                                    neighbours.filter { (d, id) ->
                                        reaction.reactiveId == id && reaction.allowedDirection.contains(d)
                                    }
                                }

                            // combines possibilities
                            val allCombinations = possibleReactions.allCombinations()

                            // chooses one randomly
                            val usedNeighbours = allCombinations[random.nextInt(allCombinations.size)].map {
                                simulation.moveIndex(i, it.first) to it.second
                            }

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
        }

        // filters behaviors that aren't concurrent
        val toExclude = all.filter { it.value.size > 1 }
            .flatMap { it.value - it.value[random.nextInt(it.value.size)] }.toSet()
        val toExecute = all.filter { it.value.isNotEmpty() }.flatMap { it.value } - toExclude
        toExecute.forEach { it.apply(simulation) }

        // stores count for each grain
        currentCount.forEach {
            grainCountHistory[it.key]?.add(it.value)
        }

        // count a step
        step += 1
    }

    fun reset() {
        simulation.reset()
        step = 0
        resetCount()
    }

    fun resetCount() {
        val counts = simulation.grainsCounts()
        grainCountHistory.forEach {
            it.value.clear()
            it.value.add(counts.getOrElse(it.key.id) { 0 })
        }
    }
}

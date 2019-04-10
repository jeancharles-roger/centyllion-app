package com.centyllion.model

import kotlin.random.Random

data class ApplicableBehavior(
    /** Index in the data where to apply the behavior */
    val index: Int,
    /** The behaviour to apply */
    val behaviour: Behaviour,
    /** */
    val usedNeighbours: List<Pair<Int, Int>>
) {

    fun apply(simulator: Simulator) {
        // applies main reaction
        val mainAge = if (behaviour.sourceReactive >= 0)
            simulator.ages[usedNeighbours[behaviour.sourceReactive].first] else -1
        simulator.transform(index, index, behaviour.mainProductId, mainAge)

        // applies other reaction find each neighbour for each reaction
        val reactives = usedNeighbours.sortedBy { it.second }
        val reactions = behaviour.reaction.sortedBy { it.reactiveId }

        // applies reactions
        reactions.zip(reactives).forEach { (reaction, reactive) ->
            val sourceIndex = reactive.first
            val newAge = if (reaction.sourceReactive >= 0)
                simulator.ages[usedNeighbours[reaction.sourceReactive].first] else -1
            simulator.transform(sourceIndex, sourceIndex, reaction.productId, newAge)
        }
    }
}

class Simulator(
    val model: GrainModel,
    val simulation: Simulation
) {

    val random = Random.Default

    val initialAgents: IntArray = IntArray(simulation.agents.size) { simulation.agents[it] }
    val agents: IntArray = initialAgents.copyOf()
    val ages: IntArray = IntArray(initialAgents.size) { if (initialAgents[it] >= 0) 0 else -1 }

    val currentAgents get() = if (step == 0) initialAgents else agents

    val grainCountHistory = grainsCounts().let { counts ->
        model.grains.map { it to mutableListOf(counts[it.id] ?: 0) }.toMap()
    }

    var step = 0

    private val allBehaviours = model.behaviours + model.grains.mapNotNull { it.moveBehaviour() }

    private val reactiveGrains = allBehaviours.mapNotNull { model.indexedGrains[it.mainReactiveId] }.toSet()

    fun grainAtIndex(index: Int) = model.indexedGrains[idAtIndex(index)]

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
                    transform(i, i, null)
                } else {

                    // ages grain
                    currentCount[grain] = currentCount.getOrElse(grain) { 0 } + 1
                    ageGrain(i)

                    val selected = all.getOrPut(i) { mutableListOf() }
                    if (reactiveGrains.contains(grain)) {
                        val age = ageAtIndex(i)

                        // a grain is present, a behaviour can be triggered
                        val neighbours = neighbours(i)

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
        toExecute.forEach { it.apply(this) }

        // stores count for each grain
        currentCount.forEach {
            grainCountHistory[it.key]?.add(it.value)
        }

        // count a step
        step += 1
    }

    fun reset() {
        initialAgents.copyInto(agents)
        for (i in 0 until ages.size) {
            ages[i] = if (agents[i] != -1) 0 else -1
        }

        step = 0
        resetCount()
    }

    fun resetCount() {
        val counts = grainsCounts()
        grainCountHistory.forEach {
            it.value.clear()
            it.value.add(counts.getOrElse(it.key.id) { 0 })
        }
    }

    fun saveState() {
        agents.copyInto(initialAgents)
    }

    fun indexIsFree(index: Int) = currentAgents[index] < 0

    fun idAtIndex(index: Int) = currentAgents[index]

    fun ageAtIndex(index: Int) = ages[index]

    fun ageGrain(index: Int) {
        ages[index] += 1
    }

    fun transform(sourceIndex: Int, targetIndex: Int, newId: Int?, newAge: Int = -1) {
        val age = ages[sourceIndex]
        agents[sourceIndex] = -1
        ages[sourceIndex] = -1
        agents[targetIndex] = newId ?: -1
        ages[targetIndex] = when {
            newId != null -> newAge
            else -> -1
        }
    }

    fun setIdAtIndex(index: Int, id: Int) {
        currentAgents[index] = id
        ages[index] = 0
    }

    fun resetIdAtIndex(index: Int) {
        currentAgents[index] = -1
        ages[index] = -1
    }

    fun neighbours(index: Int): List<Pair<Direction, Int>> =
        Direction.values().map { it to currentAgents[simulation.moveIndex(index, it)] }

    fun grainsCounts(): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        for (i in currentAgents) {
            if (i >= 0) {
                result[i] = 1 + (result[i] ?: 0)
            }
        }
        return result
    }

}

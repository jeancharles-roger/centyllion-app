package com.centyllion.model

import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

const val minField = 1e-15f
const val minFieldLevel = 1e-14f

val emptyFloatArray = FloatArray(0)

class Agent(val index: Int, val id: Int, val age: Int, val fields: FloatArray)

class ApplicableBehavior(
    /** Index in the data where to apply the behavior */
    val index: Int, val age: Int,
    /** The behaviour to apply */
    val behaviour: Behaviour,
    /** */
    val usedNeighbours: List<Agent>
) {

    fun ageForSource(reactive: Int) = when {
        reactive == 0 -> age
        reactive > 0 -> usedNeighbours[reactive - 1].age
        else -> 0
    }

    fun apply(simulator: Simulator) {
        // applies main reaction
        simulator.transform(index, index, behaviour.mainProductId, ageForSource(behaviour.sourceReactive))

        // applies other reaction find each neighbour for each reaction
        val reactives = usedNeighbours.sortedBy { it.id }
        val reactions = behaviour.reaction.sortedBy { it.reactiveId }

        // applies reactions
        reactions.zip(reactives).forEach { (reaction, reactive) ->
            val newAge = ageForSource(reaction.sourceReactive)
            simulator.transform(reactive.index, reactive.index, reaction.productId, newAge)
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

    val fieldMaxId = model.fields.map { it.id }.max() ?: 0

    val fields get() = if (currentFields) fields1 else fields2
    val nextFields get() = if (!currentFields) fields1 else fields2

    fun field(id: Int) = fields[id] ?: emptyFloatArray

    private var currentFields: Boolean = true

    private val fields1: Map<Int, FloatArray> = model.fields
        .map { it.id to FloatArray(simulation.agents.size) { minField } }.toMap()

    private val fields2: Map<Int, FloatArray> = model.fields
        .map { it.id to FloatArray(simulation.agents.size) { minField } }.toMap()

    val grainCountHistory = grainsCounts().let { counts ->
        model.grains.map { it to mutableListOf(counts[it.id] ?: 0) }.toMap()
    }

    val fieldAmountHistory = fieldAmounts().let { amounts ->
        model.fields.map { it to mutableListOf(amounts[it.id] ?: 0f) }.toMap()
    }

    var step = 0

    private val allBehaviours = model.behaviours + model.grains.mapNotNull { it.moveBehaviour() }

    private val speeds = allBehaviours.map { it to it.probability }.toMap().toMutableMap()

    private val reactiveGrains = allBehaviours.mapNotNull { model.grainForId(it.mainReactiveId) }.toSet()

    fun grainAtIndex(index: Int) = model.grainForId(idAtIndex(index))

    fun lastGrainsCount(): Map<Grain, Int> = grainCountHistory.map { it.key to it.value.last() }.toMap()

    fun lastFieldAmount(): Map<Field, Float> = fieldAmountHistory.map { it.key to it.value.last() }.toMap()

    fun getSpeed(behaviour: Behaviour) = speeds[behaviour] ?: 0.0

    fun setSpeed(behaviour: Behaviour, speed: Double) {
        speeds[behaviour] = speed
    }

    /** Executes one step and returns the applied behaviours and the dead indexes. */
    fun oneStep(): Pair<Collection<ApplicableBehavior>, Collection<Int>> {
        // applies agents dying process
        val currentCount = model.grains.map { it to 0 }.toMap().toMutableMap()
        val currentFieldAmounts = model.fields.map { it to 0f }.toMap().toMutableMap()

        // defines values for fields to avoid dynamic call each time
        val fields = fields
        val nextFields = nextFields

        val dead = mutableListOf<Int>()

        // all will contains index of agents as keys associated to a list of applicable behaviors
        // if an agent doesn't contain any applicable behavior it will have an empty list.
        // if an agent can't move and doesn't contain any applicable behavior, it won't be present in the map
        val all = mutableMapOf<Int, MutableList<ApplicableBehavior>>()
        for (i in 0 until simulation.dataSize) {
            val grain = grainAtIndex(i)
            if (grain != null) {
                // does the grain dies ?
                if (grain.halfLife > 0.0 && random.nextDouble() <= grain.deathProbability) {
                    // it dies, does't count
                    transform(i, i, null)
                    dead.add(i)

                } else {
                    currentCount[grain] = currentCount.getOrElse(grain) { 0 } + 1
                    ageGrain(i)

                    val selected = all[i].let {
                        if (it == null) {
                            val new = mutableListOf<ApplicableBehavior>()
                            all[i] = new
                            new
                        } else {
                            it
                        }
                    }
                    if (reactiveGrains.contains(grain)) {
                        // a grain is present, a behaviour can be triggered
                        val age = ageAtIndex(i)
                        val fieldValues = fieldsAtIndex(i)

                        // find all neighbours
                        val neighbours = neighbours(i)

                        // searches for applicable behaviours and filters by probability
                        val applicable = allBehaviours.filter {
                            it.applicable(grain, age, fieldValues, neighbours) && random.nextDouble() < speeds[it]!!
                        }

                        // selects behaviour if any is applicable
                        if (applicable.isNotEmpty()) {
                            // selects one at random
                            val behaviour = applicable[random.nextInt(applicable.size)]

                            // for each reactions, find all possible reactives
                            val possibleReactions = behaviour.reaction
                                .map { reaction ->
                                    neighbours.filter { (d, a) ->
                                        reaction.reactiveId == a.id && reaction.allowedDirection.contains(d)
                                    }
                                }

                            // combines possibilities
                            val allCombinations = possibleReactions.allCombinations()

                            val chosenCombination =  when {
                                // optimized case when no field is used for the behaviour
                                model.fields.isEmpty() || !behaviour.fieldInfluenced -> {
                                    random.nextInt(allCombinations.size)
                                }
                                else -> {
                                    // computes weight of each combination by adding the influence of all neighbours for a combination
                                    val influence = FloatArray(allCombinations.size) { c ->
                                        var sum = 0f
                                        allCombinations[c].forEach { (_, agent) ->
                                            behaviour.fieldInfluences.forEach { influence ->
                                                val sourceField = fieldValues.find { it.first == influence.key }?.second ?: 0f
                                                val delta = log10(agent.fields[influence.key]) - log10(sourceField)
                                                sum += delta * influence.value
                                            }
                                        }
                                        sum
                                    }

                                    val min = influence.min() ?: 0f
                                    var current = 0f
                                    for (index in influence.indices) {
                                        // translates influence to positive float and to the power of 6 for a stronger effect
                                        val translated = (influence[index] - min + 1f).pow(6)
                                        // computes the total value
                                        influence[index] = current + translated
                                        current += translated
                                    }

                                    // chooses one randomly influenced by the fields
                                    val probability = random.nextDouble(current.toDouble())
                                    influence.indexOfFirst { probability < it }
                                }
                            }
                            // registers behaviour for for concurrency
                            val usedNeighbours = allCombinations[chosenCombination].map { it.second }
                            val behavior = ApplicableBehavior(i, ages[i], behaviour, usedNeighbours)
                            selected.add(behavior)
                            usedNeighbours.forEach { all.getOrPut(it.index) { mutableListOf() }.add(behavior) }
                        }
                    }
                }
            }

            // applies field diffusion
            model.fields.forEach { field ->
                val current = fields[field.id]
                val next = nextFields[field.id]
                val count = field.allowedDirection.size

                var permeableSum = 0f
                for (allowed in field.allowedDirection) {
                    val index = simulation.moveIndex(i, allowed)
                    permeableSum += (grainAtIndex(index)?.fieldPermeable?.get(field.id) ?: 1f) ?: 1f
                }

                if (next != null && current != null) {
                    var diffusionSum = 0f
                    for (opposite in field.oppositeDirections) {
                        val index = simulation.moveIndex(i, opposite)
                        val permeable = (grainAtIndex(index)?.fieldPermeable?.get(field.id) ?: 1f) ?: 1f
                        diffusionSum += current[index] * field.speed * permeable
                    }

                    val level =
                        // current value cut down
                        current[i] * (1.0f - field.speed * permeableSum / count) +
                        // adding or removing from current agent if any
                        (grain?.fieldProductions?.get(field.id) ?: 0f) +
                        // diffusion from agent around using opposite directions
                        diffusionSum / count

                    next[i] = (level * (1f - field.deathProbability)).coerceIn(minField, 1f)
                    // count current field amounts
                    currentFieldAmounts[field] = (currentFieldAmounts[field] ?: 0f) + next[i]
                }
            }
        }

        // filters behaviors that are concurrent
        val toExclude = all.values.filter { it.size > 1 }.flatMap { it - it[random.nextInt(it.size)] }
        val toExecute = (all.values.flatten() - toExclude).toSet()
        toExecute.forEach { it.apply(this) }

        // stores count for each grain
        currentCount.forEach {
            grainCountHistory[it.key]?.add(it.value)
        }

        // stores field amounts
        currentFieldAmounts.forEach {
            fieldAmountHistory[it.key]?.add(it.value)
        }

        // swap fields
        currentFields = !currentFields

        // count a step
        step += 1

        return toExecute to dead
    }

    fun reset() {
        initialAgents.copyInto(agents)
        for (i in ages.indices) {
            ages[i] = if (agents[i] != -1) 0 else -1
            fields.forEach { it.value[i] = minField }
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

    fun idAtIndex(index: Int) = currentAgents[index]

    fun ageAtIndex(index: Int) = ages[index]

    fun ageGrain(index: Int) {
        ages[index] += 1
    }

    fun fieldsAtIndex(index: Int) = fields.map { it.key to it.value[index] }

    fun transform(sourceIndex: Int, targetIndex: Int, newId: Int?, newAge: Int = -1) {
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

    /** Returns all neighbours agents in all directions */
    fun neighbours(index: Int): List<Pair<Direction, Agent>> {
        val all = Direction.values()
        return List(all.size) { i ->
            val direction = all[i]
            direction to simulation.moveIndex(index, direction).let { id ->
                val fieldValues = if (model.fields.isEmpty()) emptyFloatArray else FloatArray(fieldMaxId + 1) {field(it)[id] }
                Agent(id, currentAgents[id], ages[id], fieldValues)
            }
        }
    }

    fun grainsCounts(): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        for (i in currentAgents) {
            if (i >= 0) {
                result[i] = 1 + (result[i] ?: 0)
            }
        }
        return result
    }

    fun fieldAmounts(): Map<Int, Float> = fields.map { it.key to it.value.sum() }.toMap()
}

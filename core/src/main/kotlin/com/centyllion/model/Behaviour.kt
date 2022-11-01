package com.centyllion.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
data class Behaviour(
    override val uuid: String = UUID.randomUUID().toString(),
    override val name: String = "",
    override val description: String = "",
    val probability: Double = 1.0,
    val agePredicate: Predicate<Int> = Predicate(Operator.GreaterThanOrEquals, 0),
    val fieldPredicates: List<Pair<Int, Predicate<Float>>> = emptyList(),
    val mainReactiveId: Int = -1, val mainProductId: Int = -1, val sourceReactive: Int = -1,
    val fieldInfluences: Map<Int, Float> = emptyMap(),
    val reaction: List<Reaction> = emptyList()
): ModelElement {

    @Transient
    val reactiveGrainIds = buildList {
        add(mainReactiveId)
        reaction.forEach { add(it.reactiveId) }
    }

    @Transient
    val productGrainIds = buildList {
        add(mainProductId)
        reaction.forEach { add(it.productId) }
    }

    @Transient
    val fieldInfluenced = fieldInfluences.any { it.value != 0f }

    fun reactionIndex(reaction: Reaction) = this.reaction.identityFirstIndexOf(reaction)

    fun updateReaction(old: Reaction, new: Reaction): Behaviour {
        val index = reactionIndex(old)
        if (index < 0) return this

        val newBehaviours = reaction.toMutableList()
        newBehaviours[index] = new
        return copy(reaction = newBehaviours)
    }

    fun dropReaction(reaction: Reaction): Behaviour {
        val index = reactionIndex(reaction)
        if (index < 0) return this

        val newReactions = this.reaction.toMutableList()
        // removes the field
        newReactions.removeAt(index)

        return copy(reaction = newReactions)
    }

    fun fieldPredicateIndex(predicate: Pair<Int, Predicate<Float>>) = fieldPredicates.identityFirstIndexOf(predicate)

    fun updateFieldPredicate(old: Pair<Int, Predicate<Float>>, new: Pair<Int, Predicate<Float>>): Behaviour {
        val index = fieldPredicateIndex(old)
        if (index < 0) return this

        val newList = fieldPredicates.toMutableList()
        newList[index] = new
        return copy(fieldPredicates = newList)
    }

    fun dropFieldPredicate(predicate: Pair<Int, Predicate<Float>>): Behaviour {
        val index = fieldPredicateIndex(predicate)
        if (index < 0) return this

        val newList = fieldPredicates.toMutableList()
        // removes the field
        newList.removeAt(index)

        return copy(fieldPredicates = newList)
    }

    fun updateFieldInfluence(id: Int, value: Float): Behaviour {
        val newFields = fieldInfluences.toMutableMap()
        newFields[id] = value
        return copy(fieldInfluences = newFields)
    }

    /** Is behavior applicable for given [grain], [age] and [neighbours] ? */
    fun applicable(
        grain: Grain, age: Int, fields: List<Pair<Int, Float>>, neighbours: List<Pair<Direction, Agent>>
    ): Boolean {
        // checks main reactive and age
        if (mainReactiveId != grain.id || !agePredicate.check(age)) return false
        // checks field predicate
        for (p in fieldPredicates) {
            val value = (fields.find { it.first == p.first }?.second ?: 0f).flatten(minFieldLevel)
            if (!p.second.check(value)) return false
        }
        // checks reactions
        for (r in reaction) {
            if (r.allowedDirection.none { d ->
                neighbours.any { it.first == d && it.second.reactiveId == r.reactiveId }
            }) return false
        }
        return true
    }

    fun usedGrains(model: GrainModel) =
        (reaction.flatMap { listOf(it.reactiveId, it.productId) } + mainReactiveId + mainProductId)
            .filter { it >= 0 }.mapNotNull { model.grainForId(it) }.toSet()

}
package com.centyllion.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GrainModel(
    override val uuid: String = UUID.randomUUID().toString(),
    override val name: String = "",
    override val description: String = "",
    val grains: List<Grain> = emptyList(),
    val behaviours: List<Behaviour> = emptyList(),
    val fields: List<Field> = emptyList()
): ModelElement {

    fun findWithUuid(uuid: String): ModelElement? {
        if (this.uuid == uuid) return this
        fields.find { it.uuid == uuid }?.let { return it }
        grains.find { it.uuid == uuid }?.let { return it }
        behaviours.find { it.uuid == uuid }?.let { return it }
        return null
    }

    fun grainForId(id: Int) = grains.find { it.id == id }

    fun fieldForId(id: Int) = fields.find { it.id == id }

    fun availableGrainName(prefix: String = "Grain"): String = availableName(grains.map(Grain::name), prefix)

    fun availableGrainId(): Int = availableId(grains.map(Grain::id))

    fun availableColor() = availableColor(grains.map(Grain::color) + fields.map(Field::color))

    fun grainIndex(grain: Grain) = grains.identityFirstIndexOf(grain)

    fun newGrain(prefix: String = "Grain") = Grain(
        id = availableGrainId(),
        name = availableGrainName(prefix),
        color = availableColor()
    )

    fun updateGrain(old: Grain, new: Grain): GrainModel {
        val grainIndex = grainIndex(old)
        if (grainIndex < 0) return this

        val newGrains = grains.toMutableList()
        newGrains[grainIndex] = new
        return copy(grains = newGrains)
    }

    fun dropGrain(grain: Grain): GrainModel {
        val index = grainIndex(grain)
        if (index < 0) return this

        val newGrains = grains.toMutableList()
        // removes the grain
        newGrains.removeAt(index)

        // clears reference to this grain in behaviours
        val newBehaviours = behaviours.map { behaviour ->
            val new = behaviour.copy(
                mainReactiveId = if (grain.id == behaviour.mainReactiveId) -1 else behaviour.mainReactiveId,
                mainProductId = if (grain.id == behaviour.mainProductId) -1 else behaviour.mainProductId,
                reaction = behaviour.reaction.map {
                    val newReaction = it.copy(
                        reactiveId = if (grain.id == it.reactiveId) -1 else it.reactiveId,
                        productId = if (grain.id == it.productId) -1 else it.productId
                    )
                    if (newReaction == it) it else newReaction
                }
            )
            if (new == behaviour) behaviour else new
        }

        return copy(grains = newGrains, behaviours = newBehaviours)
    }

    fun availableFieldId(): Int = availableId(fields.map(Field::id))

    fun availableFieldName(prefix: String = "Field"): String = availableName(fields.map(Field::name), prefix)

    fun newField(prefix: String = "Field") = Field(
        id = availableFieldId(),
        name = availableFieldName(prefix),
        color = availableColor()
    )

    fun fieldIndex(field: Field) = fields.identityFirstIndexOf(field)

    fun updateField(old: Field, new: Field): GrainModel {
        val newFields = fields.toMutableList()
        newFields[fieldIndex(old)] = new
        return copy(fields = newFields)
    }

    fun dropField(field: Field): GrainModel {
        val index = fieldIndex(field)
        if (index < 0) return this

        val fields = fields.toMutableList()
        // removes the field
        fields.removeAt(index)

        // clears reference to this field in grains
        val newGrains = grains.map { grain ->
            val new = grain.copy(
                fieldProductions = grain.fieldProductions.filter { it.key != field.id },
                fieldInfluences = grain.fieldInfluences.filter { it.key != field.id },
                fieldPermeable = grain.fieldPermeable.filter { it.key != field.id }
            )
            if (new == grain) grain else new
        }

        val newBehaviours = behaviours.map { behaviour ->
            val new = behaviour.copy(fieldInfluences = behaviour.fieldInfluences.filter { it.key != field.id })
            if (new == behaviour) behaviour else new
        }

        return copy(fields = fields, grains = newGrains, behaviours = newBehaviours)
    }

    fun availableBehaviourName(prefix: String = "Behaviour"): String =
        availableName(behaviours.map(Behaviour::name), prefix)

    fun newBehaviour(prefix: String = "Behaviour") = (grains.firstOrNull()?.id ?: -1).let {
        Behaviour(name = availableBehaviourName(prefix), mainReactiveId = it, mainProductId = it, sourceReactive = 0)
    }

    fun behaviourIndex(behaviour: Behaviour) = behaviours.identityFirstIndexOf(behaviour)

    fun updateBehaviour(old: Behaviour, new: Behaviour): GrainModel {
        val behaviourIndex = behaviourIndex(old)
        if (behaviourIndex < 0) return this

        val newBehaviours = behaviours.toMutableList()
        newBehaviours[behaviourIndex] = new
        return copy(behaviours = newBehaviours)
    }

    fun dropBehaviour(behaviour: Behaviour): GrainModel {
        val index = behaviourIndex(behaviour)
        if (index < 0) return this

        val newBehaviours = behaviours.toMutableList()
        // removes the field
        newBehaviours.removeAt(index)

        return copy(behaviours = newBehaviours)
    }

    /**
     * Does the given [grain] may change count over the course of a simulation ?
     */
    fun doesGrainCountCanChange(grain: Grain): Boolean {
        return grain.halfLife > 0 || behaviours.map {
            val reactiveCount = it.reaction.map { if (it.reactiveId == grain.id) 1 else 0 }.sum() + (if (it.mainReactiveId == grain.id) 1 else 0)
            val productCount = it.reaction.map { if (it.productId == grain.id) 1 else 0 }.sum() + (if (it.mainProductId == grain.id) 1 else 0)
            reactiveCount != productCount
        }.fold(false) { a, p -> a || p }
    }

    companion object {
        fun new(name: String) = GrainModel(name = name)

    }
}
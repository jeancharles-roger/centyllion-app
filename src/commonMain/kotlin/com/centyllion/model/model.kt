package com.centyllion.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.pow

enum class Direction {
    Left, Right, Up, Down, LeftUp, RightUp, LeftDown, RightDown, Front, Back
}

val defaultDirection = setOf(Direction.Left, Direction.Up, Direction.Right, Direction.Down)

val firstDirections = setOf(Direction.Left, Direction.Up, Direction.Right, Direction.Down)
val extendedDirections = setOf(Direction.LeftUp, Direction.LeftDown, Direction.RightUp, Direction.RightDown)

val emptyModel = GrainModel("")
val emptyDescription = DescriptionInfo()
val emptyGrainModelDescription = GrainModelDescription("", info = emptyDescription, tags = "", model = emptyModel)
val emptySimulation = createSimulation("")
val emptySimulationDescription =
    SimulationDescription("", info = emptyDescription, modelId = "", thumbnailId = null, simulation = emptySimulation)

fun <T> List<T>.identityFirstIndexOf(value: T): Int {
    val identity = this.indexOfFirst { it === value }
    return if (identity < 0) this.indexOf(value) else identity
}

enum class Operator(val label: String) {
    Equals("="), NotEquals("!="), LessThan("<"), LessThanOrEquals("<="), GreaterThan(">"), GreaterThanOrEquals(">=")
}

@Serializable
data class Predicate<C : Comparable<C>>(
    val op: Operator,
    val constant: C
) {
    fun check(value: C) = when (op) {
        Operator.Equals -> value == constant
        Operator.NotEquals -> value != constant
        Operator.LessThan -> value < constant
        Operator.LessThanOrEquals -> value <= constant
        Operator.GreaterThan -> value > constant
        Operator.GreaterThanOrEquals -> value >= constant
    }
}

@Serializable
data class Asset3d(
    val url: String, val opacity: Double = 1.0,
    val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0,
    val xScale: Double = 1.0, val yScale: Double = 1.0, val zScale: Double = 1.0,
    val xRotation: Double = 0.0, val yRotation: Double = 0.0, val zRotation: Double = 0.0
)

@Serializable
data class Field(
    val id: Int = 0,
    val name: String = "",
    val color: String = "SkyBlue",
    val description: String = "",
    val speed: Float = 0.8f,
    val halfLife: Int = 10,
    val allowedDirection: Set<Direction> = defaultDirection
) {
    /** Label for grain */
    fun label(long: Boolean = false) = when {
        long && description.isNotEmpty() -> description
        name.isNotEmpty() -> name
        else -> "$id"
    }

    @Transient
    val deathProbability = if (halfLife > 0) 1f - 2f.pow(-1f / halfLife) else 0f
}

@Serializable
data class Grain(
    val id: Int = 0,
    val name: String = "",
    val color: String = "red",
    val icon: String = "square-full",
    val size: Double = 1.0,
    val description: String = "",
    val halfLife: Int = 0,
    val movementProbability: Double = 1.0,
    val allowedDirection: Set<Direction> = defaultDirection,
    val fieldProductions: Map<Int, Float> = emptyMap(),
    val fieldInfluences: Map<Int, Float> = emptyMap(),
    val fieldPermeable: Map<Int, Float> = emptyMap()
) {
    /** Label for grain */
    fun label(long: Boolean = false) = when {
        long && description.isNotEmpty() -> description
        name.isNotEmpty() -> name
        else -> "$id"
    }

    /** True if an agent of this Grain can move */
    @Transient
    val canMove = movementProbability > 0.0 && allowedDirection.isNotEmpty()

    @Transient
    val deathProbability = if (halfLife > 0) 1.0 - 2.0.pow(-1.0 / halfLife) else 0.0

    @Transient
    val iconString = solidIconNames[icon] ?: "\uf45c"

    fun updateFieldProduction(id: Int, value: Float): Grain {
        val newFields = fieldProductions.toMutableMap()
        newFields[id] = value
        return copy(fieldProductions = newFields)
    }

    fun updateFieldInfluence(id: Int, value: Float): Grain {
        val newFields = fieldInfluences.toMutableMap()
        newFields[id] = value
        return copy(fieldInfluences = newFields)
    }

    fun updateFieldPermeable(id: Int, value: Float): Grain {
        val newFields = fieldPermeable.toMutableMap()
        newFields[id] = value
        return copy(fieldPermeable = newFields)
    }

    fun moveBehaviour() =
        if (canMove) Behaviour(
            "Move ${label()}", probability = movementProbability, mainReactiveId = id, sourceReactive = -1,
            reaction = listOf(Reaction(productId = id, sourceReactive = 0, allowedDirection = allowedDirection)),
            fieldInfluences = fieldInfluences
        )
        else null

}

@Serializable
data class Reaction(
    val reactiveId: Int = -1,
    val productId: Int = -1,
    val sourceReactive: Int = -1,
    val allowedDirection: Set<Direction> = defaultDirection
) {
    fun validForModel(model: GrainModel) = model.indexedGrains.containsKey(reactiveId) &&
            if (productId >= 0) model.indexedGrains.containsKey(productId) else true
}

@Serializable
data class Behaviour(
    val name: String = "",
    val description: String = "",
    val probability: Double = 1.0,
    val agePredicate: Predicate<Int> = Predicate(Operator.GreaterThanOrEquals, 0),
    val fieldPredicates: List<Pair<Int, Predicate<Float>>> = emptyList(),
    val mainReactiveId: Int = -1, val mainProductId: Int = -1, val sourceReactive: Int = -1,
    val fieldInfluences: Map<Int, Float> = emptyMap(),
    val reaction: List<Reaction> = emptyList()
) {
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
        grain: Grain,
        age: Int,
        fields: List<Pair<Int, Float>>,
        neighbours: List<Pair<Direction, Agent>>
    ): Boolean =
        mainReactiveId == grain.id && agePredicate.check(age) &&
                fieldPredicates.fold(true) { a, p ->
                    a && p.second.check(
                        (fields.firstOrNull { it.first == p.first }?.second ?: 0f).flatten(minFieldLevel)
                    )
                } &&
                reaction.fold(true) { a, r ->
                    a && r.allowedDirection.any { d ->
                        neighbours.any { it.first == d && it.second.id == r.reactiveId }
                    }
                }

    fun usedGrains(model: GrainModel) =
        (reaction.flatMap { listOf(it.reactiveId, it.productId) } + mainReactiveId + mainProductId)
            .filter { it >= 0 }.mapNotNull { model.indexedGrains[it] }.toSet()

    fun validForModel(model: GrainModel) = name.isNotBlank() && probability >= 0.0 && probability <= 1.0 &&
            mainReactiveId >= 0 &&
            reaction.fold(true) { a, r -> a && r.validForModel(model) }
}

@Serializable
data class Position(
    val x: Int, val y: Int, val z: Int = 0
) {
    fun move(direction: Direction, step: Int = 1) = when (direction) {
        Direction.Left -> copy(x = x + step)
        Direction.Right -> copy(x = x - step)
        Direction.Up -> copy(y = y - step)
        Direction.Down -> copy(y = y + step)
        Direction.Front -> copy(z = z - step)
        Direction.LeftUp -> copy(x = x + step, y = y - step)
        Direction.RightUp -> copy(x = x - step, y = y - step)
        Direction.LeftDown -> copy(x = x + step, y = y + step)
        Direction.RightDown -> copy(x = x - step, y = y + step)
        Direction.Back -> copy(z = z + step)
    }
}

@Serializable
data class GrainModel(
    val name: String = "",
    val description: String = "",
    val grains: List<Grain> = emptyList(),
    val behaviours: List<Behaviour> = emptyList(),
    val fields: List<Field> = emptyList()
) {
    @Transient
    val indexedGrains: Map<Int, Grain> = grains.map { it.id to it }.toMap()

    fun availableGrainName(prefix: String = "Grain"): String = availableName(grains.map(Grain::name), prefix)

    fun availableGrainId(): Int = availableId(grains.map(Grain::id))

    fun availableColor() = availableColor(grains.map(Grain::color) + fields.map(Field::color))

    fun grainIndex(grain: Grain) = grains.identityFirstIndexOf(grain)

    fun newGrain(prefix: String = "Grain") = Grain(availableGrainId(), availableGrainName(prefix), availableColor())

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

    fun newField(prefix: String = "Field") = Field(availableFieldId(), availableFieldName(prefix), availableColor())

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

    fun availableBehaviourName(prefix: String = "Behaviour"): String = availableName(fields.map(Field::name), prefix)

    fun newBehaviour(prefix: String = "Behaviour") = Behaviour(availableBehaviourName(prefix))

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
}

fun emptyList(size: Int): List<Int> = ArrayList<Int>(size).apply { repeat(size) { add(-1) } }

fun createSimulation(
    name: String = "",
    description: String = "",
    width: Int = 100,
    height: Int = 100,
    depth: Int = 1,
    agents: List<Int> = emptyList(width * height * depth),
    assets: List<Asset3d> = emptyList()
) = Simulation(name, description, width, height, depth, agents, assets)

@Serializable
data class Simulation(
    val name: String, val description: String,
    val width: Int, val height: Int, val depth: Int,
    val agents: List<Int>, val assets: List<Asset3d> = emptyList()
) {

    fun assetIndex(asset: Asset3d) = assets.identityFirstIndexOf(asset)

    fun updateAsset(old: Asset3d, new: Asset3d): Simulation {
        val newAssets = assets.toMutableList()
        newAssets[assetIndex(old)] = new
        return copy(assets = newAssets)
    }

    fun dropAsset(asset: Asset3d): Simulation {
        val index = assetIndex(asset)
        if (index < 0) return this

        val assets = assets.toMutableList()
        // removes the asset
        assets.removeAt(index)

        return copy(assets = assets)
    }

    @Transient
    val levelSize = width * height

    @Transient
    val dataSize = levelSize * depth

    val valid
        get() = width > 0 && height > 0 && depth > 0

    /** Move [index] on given [direction] of [step] cases, whatever the index, it will always remains inside the simulation. */
    fun moveIndex(index: Int, direction: Direction, step: Int = 1): Int {
        var z = index / levelSize
        val yRest = index - z * levelSize
        var y = yRest / width
        var x = yRest - y * width

        when (direction) {
            Direction.Left -> x = (x + width - step) % width
            Direction.Right -> x = (x + step) % width
            Direction.Up -> y = (y + height - step) % height
            Direction.Down -> y = (y + step) % height
            Direction.LeftUp -> {
                x = (x + width - step) % width
                y = (y + height - step) % height
            }
            Direction.LeftDown -> {
                x = (x + width - step) % width
                y = (y + step) % height
            }
            Direction.RightUp -> {
                x = (x + step) % width
                y = (y + height - step) % height
            }
            Direction.RightDown -> {
                x = (x + step) % width
                y = (y + step) % height
            }
            Direction.Front -> z = (z + depth - step) % depth
            Direction.Back -> z = (z + step) % depth
        }

        if (x < 0) x += width
        if (y < 0) y += height
        if (z < 0) z += depth

        return z * levelSize + y * width + x
    }

    /** Transform given [position] to index. */
    fun toIndex(position: Position) = toIndex(position.x, position.y, position.z)

    fun toIndex(x: Int, y: Int, z: Int = 0) = z * (height * width) + y * width + x

    fun indexInside(index: Int) = index in 0 until dataSize

    /** Transforms index to position, only to be used for printing, it's slow */
    fun toPosition(index: Int): Position {
        val zDelta = height * width
        val z = index / zDelta
        val yRest = index - z * zDelta
        val y = yRest / width
        return Position(yRest - y * width, y, z)
    }

    fun positionInside(position: Position) =
        position.z in 0 until depth && position.y in 0 until height && position.x in 0 until width

    fun positionInside(x: Int, y: Int, z: Int = 0) =
        z in 0 until depth && y in 0 until height && x in 0 until width

    /** Cleans the simulation to remove non existing grains */
    fun cleaned(model: GrainModel): Simulation {
        val newAgents = agents.map {
            val new = when {
                it < 0 -> -1
                model.indexedGrains[it] == null -> -1
                else -> it
            }
            new
        }
        return if (newAgents != agents) copy(agents = newAgents) else this
    }
}

interface Description: Ided {
    val name: String
    val icon: String

    val label get() = if (name.isNotEmpty()) name else id.drop(id.lastIndexOf("-") + 1)
}

@Serializable
data class DescriptionInfo(
    val user: User? = null,
    val createdOn: String = "",
    val lastModifiedOn: String = "",
    val readAccess: Boolean = false,
    val cloneAccess: Boolean = false
) {
    val public get() = readAccess || cloneAccess
}

@Serializable
data class GrainModelDescription(
    override val id: String,
    val info: DescriptionInfo,
    val tags: String,
    val model: GrainModel
) : Description {

    @Transient
    override val name = model.name

    @Transient
    override val icon = "boxes"
}

@Serializable
data class SimulationDescription(
    override val id: String,
    val info: DescriptionInfo,
    val modelId: String,
    val thumbnailId: String?,
    val simulation: Simulation
) : Description {

    @Transient
    override val name = simulation.name

    @Transient
    override val icon = "play"

    fun cleaned(model: GrainModelDescription): SimulationDescription {
        val new = simulation.cleaned(model.model)
        return if (new != simulation) copy(simulation = new) else this
    }
}

@Serializable
data class FeaturedDescription(
    override val id: String,
    val date: String,
    val thumbnailId: String?,
    val modelId: String,
    val simulationId: String,
    val authorId: String,
    override val name: String,
    val description: String,
    val authorName: String
) : Description {

    @Transient
    override val icon = "star"
}

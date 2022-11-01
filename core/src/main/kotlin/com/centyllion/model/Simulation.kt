package com.centyllion.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Simulation(
    val name: String, val description: String,
    val width: Int, val height: Int, val depth: Int,
    val agents: List<Int>, val assets: List<Asset3d> = emptyList(),
    val settings: SimulationSettings = SimulationSettings()
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
                model.grainForId(it) == null -> -1
                else -> it
            }
            new
        }
        return if (newAgents != agents) copy(agents = newAgents) else this
    }

    companion object {
        val empty = create()

        fun create(
            name: String = "",
            description: String = "",
            width: Int = 100,
            height: Int = 100,
            depth: Int = 1,
            agents: List<Int> = List(width * height * depth) { -1 },
            assets: List<Asset3d> = emptyList()
        ) = Simulation(name, description, width, height, depth, agents, assets)

    }

}
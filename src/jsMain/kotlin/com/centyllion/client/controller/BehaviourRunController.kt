package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.Simulator
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow
import kotlin.properties.Delegates.observable

class BehaviourRunController(
    behaviour: Behaviour, simulator: Simulator,
    var onSpeedChange: (Behaviour, Double) -> Unit = { _, _ -> }
) : Controller<Behaviour, Simulator, Column> {

    override var data: Behaviour by observable(behaviour) { _, old, new ->
        if (old != new) refresh()
    }

    override var context: Simulator by observable(simulator) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly = false

    val titleLabel = Label(behaviour.name)
    val descriptionLabel = p().apply { text = behaviour.description }

    val header = Level(listOf(titleLabel), grains(), mobile = true)

    val speedValue = Help(data.probability.toString())

    val speedSlider = Slider(toSlider(context.getSpeed(data)), "1", "10", "0.01")
    { _, new ->
        val probability = toProbability(new)
        onSpeedChange(data, probability)
        speedValue.text = format(probability)
    }

    val speedColumns = Columns(Column(speedValue, size = ColumnSize.S1), Column(speedSlider), mobile = true)

    val body = Media(center = listOf(header, descriptionLabel, speedColumns))

    override val container = Column(body, size = ColumnSize.Full)

    fun toSlider(p: Double) = (10.0.pow(p)).toString()

    fun toProbability(value: String) = log10(value.toDouble())

    fun format(value: Double) = value.toString().let {
        it.substring(0, min(it.lastIndexOf(".") + 3, it.length))
    }

    fun grainIcon(id: Int) = context.model.indexedGrains[id].let {
        if (it != null) Icon(it.icon).apply { root.style.color = it.color } else Icon("times-circle")
    }

    fun grainIcons(mainId: Int, reactionIds: List<Int>) = (listOf(mainId) + reactionIds)
        .map { grainIcon(it) }.fold(emptyList<Icon>()) { a, e ->
            if (a.isNotEmpty()) a + Icon("plus", color = TextColor.GreyLight) + e else a + e
        }

    private fun grains(): List<BulmaElement> {
        val reactives = grainIcons(data.mainReactiveId, data.reaction.map { it.reactiveId }).toTypedArray()
        val products = grainIcons(data.mainProductId, data.reaction.map { it.productId }).toTypedArray()
        return listOf(div(*reactives)) + Icon("arrow-right", color = TextColor.Primary) + div(*products)
    }

    override fun refresh() {
        titleLabel.text = data.name
        descriptionLabel.text = data.description
        val speed = context.getSpeed(data)
        speedValue.text = format(speed)
        speedSlider.value = toSlider(speed)
        header.center = grains()
    }

}

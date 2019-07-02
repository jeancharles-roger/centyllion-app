package com.centyllion.client.controller.model

import bulma.BulmaElement
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Controller
import bulma.Div
import bulma.Help
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.Media
import bulma.Slider
import bulma.TextColor
import com.centyllion.model.Behaviour
import com.centyllion.model.Simulator
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow
import kotlin.properties.Delegates.observable

class BehaviourRunController(
    behaviour: Behaviour, simulator: Simulator,
    var onSpeedChange: (Behaviour, Double) -> Unit = { _, _ -> }
) : Controller<Behaviour, Simulator, Media> {

    override var data: Behaviour by observable(behaviour) { _, old, new ->
        if (old != new) refresh()
    }

    override var context: Simulator by observable(simulator) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly = false

    val titleLabel = Label(behaviour.name)

    val grains = Level(center = grains(), mobile = true)

    val speedValue = Help(data.probability.toString())

    val speedSlider = Slider(toSlider(context.getSpeed(data)), "1", "10", "0.01")
    { _, new ->
        val probability = toProbability(new)
        onSpeedChange(data, probability)
        speedValue.text = format(probability)
    }

    val speedColumns = Columns(Column(speedValue, size = ColumnSize.S1), Column(speedSlider), mobile = true)

    override val container = Media(center = listOf(titleLabel, grains, speedColumns)).apply {
        root.classList.add("is-outlined")
    }

    fun toSlider(p: Double) = (10.0.pow(p)).toString()

    fun toProbability(value: String) = log10(value.toDouble())

    fun format(value: Double) = value.toString().let {
        it.substring(0, min(it.lastIndexOf("") + 3, it.length))
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
        return listOf(Div(*reactives)) + Icon("arrow-right", color = TextColor.Primary) + Div(*products)
    }

    override fun refresh() {
        titleLabel.text = data.name
        val speed = context.getSpeed(data)
        speedValue.text = format(speed)
        speedSlider.value = toSlider(speed)
        grains.center = grains()
    }

}

package com.centyllion.client.controller.model

import bulma.Box
import bulma.BulmaElement
import bulma.Button
import bulma.Controller
import bulma.Div
import bulma.ElementColor
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.Size
import bulma.TextColor
import bulma.extension.Slider
import bulma.extension.Switch
import bulma.textButton
import com.centyllion.client.toFixed
import com.centyllion.model.Behaviour
import com.centyllion.model.Simulator
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.properties.Delegates.observable

class BehaviourRunController(
    behaviour: Behaviour, simulator: Simulator,
    var onValidate: (Behaviour, Double) -> Unit = { _, _ -> },
    var onSpeedChange: (Behaviour, Double) -> Unit = { _, _ -> }
) : Controller<Behaviour, Simulator, Box> {

    override var data: Behaviour by observable(behaviour) { _, old, new ->
        if (old != new) {
            activeSwitch.checked = true
            refresh()
        }
    }

    override var context: Simulator by observable(simulator) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly = false

    val activeSwitch = Switch(
        "", color = ElementColor.Success, size = Size.Small, rounded = true, checked = true
    ) { _, v ->
        val probability = if (v) toProbability(speedSlider.value) else 0.0
        onSpeedChange(data, probability)
    }

    val titleLabel = Label(behaviour.name)

    val grains = Level(center = grains(), mobile = true)

    val speedValue: Button = textButton(
        data.probability.toString(), rounded = true,
        color = ElementColor.Success, size = Size.Small
    ) { _ ->
        val probability = toProbability(speedSlider.value)
        if (probability != data.probability) onValidate(data, probability)
    }

    val speedSlider: Slider = Slider(toSlider(context.getSpeed(data)), "0", "1", "any", circle = true)
    { _, new ->
        val probability = toProbability(new)
        onSpeedChange(data, probability)
        speedValue.text = probability.toFixed()
        speedValue.disabled = probability == data.probability
        speedValue.color = if (probability == data.probability) ElementColor.Success else ElementColor.Warning
    }

    val header = Level(
        left = listOf(activeSwitch, titleLabel),
        center = listOf(speedSlider),
        right = listOf(speedValue),
        mobile = true
     )

    override val container = Box(header, grains)

    fun toSlider(p: Double) = sqrt(p).toString()

    fun toProbability(value: String) = value.toDouble().pow(2)

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
        speedValue.text = speed.toFixed()
        speedValue.disabled = speed == data.probability
        speedValue.color = if (speed == data.probability) ElementColor.Success else ElementColor.Warning
        speedSlider.value = toSlider(speed)
        grains.center = grains()
    }

}

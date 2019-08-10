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
import bulma.textButton
import com.centyllion.client.toFixed
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import kotlin.math.pow
import kotlin.properties.Delegates.observable

class BehaviourDisplayController(behaviour: Behaviour, model: GrainModel) : Controller<Behaviour, GrainModel, Box> {

    override var data: Behaviour by observable(behaviour) { _, old, new ->
        if (old != new) refresh()
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly = false

    val titleLabel = Label(behaviour.name)

    val grains = Level(center = grains(), mobile = true)

    val speedValue: Button = textButton(
        data.probability.toString(), rounded = true,
        color = ElementColor.Success, size = Size.Small
    )

    val header = Level(
        left = listOf(titleLabel),
        right = listOf(speedValue),
        mobile = true
     )

    override val container = Box(header, grains)

    fun toSlider(p: Double) = (10.0.pow(p)).toString()

    fun grainIcon(id: Int) = context.indexedGrains[id].let {
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
        val speed = data.probability
        speedValue.text = speed.toFixed()
        speedValue.disabled = speed == data.probability
        speedValue.color = if (speed == data.probability) ElementColor.Success else ElementColor.Warning
        grains.center = grains()
    }

}

package com.centyllion.client.controller.model

import bulma.*
import com.centyllion.client.controller.utils.DeleteCallbackProperty
import com.centyllion.client.page.BulmaPage
import com.centyllion.client.toFixed
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class BehaviourDisplayController(val page: BulmaPage, behaviour: Behaviour, model: GrainModel) : Controller<Behaviour, GrainModel, Box> {

    override var data: Behaviour by observable(behaviour) { _, old, new ->
        if (old != new) {
            errorIcon.hidden = data.diagnose(context, page.appContext.locale).isEmpty()
            refresh()
        }
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            errorIcon.hidden = data.diagnose(context, page.appContext.locale).isEmpty()
            refresh()
        }
    }

    override var readOnly by observable(false) { _, old, new ->
        if (old != new) {
            deleteCallbackProperty.readOnly = new
        }
    }

    val deleteCallbackProperty = DeleteCallbackProperty(null, this) { old, new ->
        old?.let { header.right -= it }
        new?.let { header.right += it }
    }
    var onDelete by deleteCallbackProperty

    val errorIcon = Icon("exclamation-triangle", color = TextColor.Danger).apply {
        hidden = data.diagnose(context, page.appContext.locale).isEmpty()
    }

    val titleLabel = Label(behaviour.name)

    val grains = Level(center = grains(), mobile = true)

    val speedValue: Button = textButton(
        data.probability.toString(), rounded = true,
        color = ElementColor.Success, size = Size.Small
    )

    val header = Level(
        left = listOf(errorIcon, titleLabel),
        right = listOf(speedValue),
        mobile = true
     )

    override val container = Box(header, grains)

    fun grainIcon(id: Int) = context.grainForId(id).let {
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

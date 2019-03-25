package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Grain
import org.w3c.dom.events.Event
import kotlin.properties.Delegates.observable

class GrainSelectController(
    grain: Grain?, grains: List<Grain>,
    var onUpdate: (old: Grain?, new: Grain?, controller: GrainSelectController) -> Unit = { _, _, _ -> }
) : Controller<Grain?, List<Grain>, Field> {

    override var data by observable(grain) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@GrainSelectController)
            refresh()
        }
    }

    override var context: List<Grain> by observable(grains) { _, old, new ->
        if (old != new) {
            select.options = options()
            this@GrainSelectController.refresh()
        }
    }

    val icon = Icon("circle")
    val button = iconButton(icon, rounded = true)

    val select = Select(options()) { _: Event, value: String ->
        val index = value.toInt()
        val newValue = if (index >= context.size) null else context[index]
        data = newValue
    }

    override val container: Field = Field(Control(button), Control(select), addons = true)

    private fun options() = context.map { Option(it.name, "${it.id}") } + Option("none", "${context.size}")

    init {
        refresh()
    }

    override fun refresh() {
        val index = data.let { if (it != null) context.indexOf(it) else select.options.lastIndex }
        select.selectedIndex = index
        icon.root.style.color = data?.color ?: "transparent"
    }

}

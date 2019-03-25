package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Grain
import org.w3c.dom.events.Event
import kotlin.properties.Delegates.observable

class GrainSelectController(
    grain: Grain?, grains: List<Grain>,
    var onUpdate: (old: Grain?, new: Grain?, controller: GrainSelectController) -> Unit = { _, _, _ -> }
) : Controller<Grain?, Field> {

    var grains = grains
        set(value) {
            field = value
            select.options = options()
        }

    override var data by observable(grain) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@GrainSelectController)
            refresh()
        }
    }

    val icon = Icon("circle")
    val button = iconButton(icon, rounded = true)

    val select = Select(options()) { _: Event, value: String ->
        val index = value.toInt()
        val newValue = if (index < 0) null else grains[index]
        data = newValue
    }

    override val container: Field = Field(Control(button), Control(select), addons = true)

    private fun options() = grains.map { Option(it.name, "${it.id}") } + Option("none", "-1")

    init {
        refresh()
    }

    override fun refresh() {
        select.options = options()
        val index = data.let { if (it != null) grains.indexOf(it) else select.options.lastIndex }
        select.selectedIndex = index
        icon.root.style.color = data?.color ?: "transparent"
    }

}

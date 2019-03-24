package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Grain
import org.w3c.dom.events.Event
import kotlin.properties.Delegates.observable

class GrainSelectController(
    grain: Grain?, grains: List<Grain>,
    var onUpdate: (old: Grain?, new: Grain?, controller: GrainSelectController) -> Unit = { _, _, _ -> }
): Controller<Grain?, Field> {

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

    val select = Select(options()) { _: Event, value: Option ->
        data = if (value.index == grains.size) null else grains[value.index]
    }

    override val container: Field = Field(Control(select))

    private fun options() = grains.map { Option(it.name, "${it.id}") } + Option("none", "-1")

    init {
        refresh()
    }

    override fun refresh() {
        val index = data.let { if (it != null) grains.indexOf(it) else select.options.lastIndex }
        select.selectedIndex = index
    }

}

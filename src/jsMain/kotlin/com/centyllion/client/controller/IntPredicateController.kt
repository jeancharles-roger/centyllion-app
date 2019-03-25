package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Operator
import com.centyllion.model.Predicate
import org.w3c.dom.events.Event
import kotlin.properties.Delegates.observable

class IntPredicateController(
    predicate: Predicate<Int>,
    var onUpdate: (old: Predicate<Int>, new: Predicate<Int>, controller: IntPredicateController) -> Unit =
        { _, _, _ -> }
): NoContextController<Predicate<Int>, Field>() {

    override var data by observable(predicate) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@IntPredicateController)
            refresh()
        }
    }

    val select = Select(Operator.values().map { Option(it.label, it.name) }) { _: Event, value: String ->
        data = data.copy(op = Operator.valueOf(value))
    }

    val value = editableIntController(data.constant) { _, new, _ ->
        data = data.copy(constant = new)
    }

    override val container: Field = Field(Control(select), value.container, addons = true)

    init {
        refresh()
    }

    override fun refresh() {
        select.selectedIndex = data.op.ordinal
        value.data = "${data.constant}"
    }

}

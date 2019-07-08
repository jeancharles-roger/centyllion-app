package com.centyllion.client.controller.model

import bulma.Control
import bulma.Field
import bulma.NoContextController
import bulma.Option
import bulma.Select
import com.centyllion.client.controller.utils.editableFloatController
import com.centyllion.model.Operator
import com.centyllion.model.Predicate
import org.w3c.dom.events.Event
import kotlin.properties.Delegates.observable

class FloatPredicateController(
    predicate: Predicate<Float>,
    var onUpdate: (old: Predicate<Float>, new: Predicate<Float>, controller: FloatPredicateController) -> Unit =
        { _, _, _ -> }
): NoContextController<Predicate<Float>, Field>() {

    override var data by observable(predicate) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@FloatPredicateController)
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            select.disabled = new
            value.readOnly = new
        }
    }

    val select = Select(Operator.values().map { Option(it.label, it.name) }, rounded = true) { _: Event, value: String ->
        data = data.copy(op = Operator.valueOf(value))
    }

    val value = editableFloatController(data.constant) { _, new, _ ->
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

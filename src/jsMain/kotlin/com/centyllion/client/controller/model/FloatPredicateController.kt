package com.centyllion.client.controller.model

import bulma.Control
import bulma.Field
import bulma.NoContextController
import bulma.Option
import bulma.Select
import com.centyllion.client.controller.utils.editableFloatController
import com.centyllion.i18n.Locale
import com.centyllion.model.Operator
import com.centyllion.model.Predicate
import org.w3c.dom.events.Event
import kotlin.properties.Delegates.observable

class FloatPredicateController(
    locale: Locale, predicate: Predicate<Float>, placeHolder: String = "", minValue: Float = Float.NEGATIVE_INFINITY, maxValue: Float = Float.POSITIVE_INFINITY,
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

    val select = Select(Operator.values().map { Option(it.label, it.name) }, rounded = true) { _: Event, value: List<Option> ->
        value.firstOrNull()?.value?.let {
            data = data.copy(op = Operator.valueOf(it))
        }
    }

    val value = editableFloatController(locale, data.constant, placeHolder, minValue, maxValue)
    { _, new, _ -> data = data.copy(constant = new) }

    override val container: Field = Field(Control(select), value.container, addons = true)

    init {
        refresh()
    }

    override fun refresh() {
        select.selectedIndex = data.op.ordinal
        value.data = "${data.constant}"
    }

}

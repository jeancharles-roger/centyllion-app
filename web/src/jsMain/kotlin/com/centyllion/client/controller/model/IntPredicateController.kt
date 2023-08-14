package com.centyllion.client.controller.model

import bulma.*
import com.centyllion.client.controller.utils.editableIntController
import com.centyllion.i18n.Locale
import com.centyllion.model.Operator
import com.centyllion.model.Predicate
import org.w3c.dom.events.Event
import kotlin.properties.Delegates.observable

class IntPredicateController(
    locale: Locale, predicate: Predicate<Int>, placeHolder: String = "", minValue: Int = Int.MIN_VALUE, maxValue: Int = Int.MAX_VALUE,
    var onUpdate: (old: Predicate<Int>, new: Predicate<Int>, controller: IntPredicateController) -> Unit =
        { _, _, _ -> }
): NoContextController<Predicate<Int>, Field>() {

    override var data by observable(predicate) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@IntPredicateController)
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

    val value = editableIntController(locale, data.constant, placeHolder, minValue, maxValue)
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

package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Controller
import bulma.Level
import com.centyllion.model.Field
import com.centyllion.model.Predicate
import kotlin.properties.Delegates.observable

class FieldPredicateController(
    value: Pair<Int, Predicate<Float>>, fields: List<Field>, min: Float = -1f, max: Float = 1f,
    var onUpdate: (old: Pair<Int, Predicate<Float>>, new: Pair<Int, Predicate<Float>>, controller: FieldPredicateController) -> Unit = { _, _, _ -> }
) : Controller<Pair<Int, Predicate<Float>>, List<Field>, Column> {

    override var data: Pair<Int, Predicate<Float>> by observable(value) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@FieldPredicateController)
            refresh()
        }
    }

    val field get() = context.find { it.id == data.first }

    override var context: List<Field> by observable(fields) { _, old, new ->
        if (old != new) {
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            //valueSlider.disabled = new
        }
    }

    val predicateController = FloatPredicateController(value.second) { old, new, _ ->
        if (old != new) data = data.first to new
    }


    override val container = Column(
        Level(left = listOf(), right = listOf(predicateController)), size = ColumnSize.Full
    )

    override fun refresh() {
    }

}

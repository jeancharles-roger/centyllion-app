package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Controller
import bulma.ElementColor
import bulma.Icon
import bulma.Level
import bulma.Size
import bulma.iconButton
import com.centyllion.model.Field
import com.centyllion.model.Predicate
import kotlin.properties.Delegates.observable

typealias FieldPredicate = Pair<Int, Predicate<Float>>

class FieldPredicateController(
    value: FieldPredicate, fields: List<Field>,
    var onUpdate: (old: FieldPredicate, new: FieldPredicate, controller: FieldPredicateController) -> Unit = { _, _, _ -> },
    var onDelete: (FieldPredicate, controller: FieldPredicateController) -> Unit = { _, _ -> }
) : Controller<FieldPredicate, List<Field>, Column> {

    override var data: FieldPredicate by observable(value) { _, old, new ->
        if (old != new) {
            fieldController.data = field
            predicateController.data = new.second
            onUpdate(old, new, this@FieldPredicateController)
            refresh()
        }
    }

    val field get() = context.find { it.id == data.first } ?: context.first()

    override var context: List<Field> by observable(fields) { _, old, new ->
        if (old != new) {
            fieldController.context = new
            fieldController.data = field
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            fieldController.readOnly = new
            predicateController.readOnly = new
        }
    }

    val fieldController = FieldSelectController(field, context) { old, new, _ ->
        if (old != new) data = new.id to data.second
    }

    val predicateController = FloatPredicateController(value.second) { old, new, _ ->
        if (old != new) data = data.first to new
    }

    val delete = iconButton(Icon("times", Size.Small), ElementColor.Danger, true, size = Size.Small)
    {
        onDelete(this.data, this@FieldPredicateController)
    }


    override val container = Column(
        Level(
            left = listOf(fieldController),
            center = listOf(predicateController),
            right = listOf(delete)
        ),
        size = ColumnSize.Full
    )

    override fun refresh() {
        fieldController.refresh()
        predicateController.refresh()
    }

}

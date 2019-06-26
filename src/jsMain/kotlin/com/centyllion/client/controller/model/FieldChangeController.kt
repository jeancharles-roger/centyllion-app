package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Controller
import com.centyllion.client.controller.utils.editableFloatController
import com.centyllion.model.Field
import kotlin.properties.Delegates.observable

class FieldChangeController(
    value: Pair<Int, Float>, fields: List<Field>,
    var onUpdate: (old: Pair<Int, Float>, new: Pair<Int, Float>, controller: FieldChangeController) -> Unit = { _, _, _ -> }
) : Controller<Pair<Int, Float>, List<Field>, Column> {

    override var data: Pair<Int, Float> by observable(value) { _, old, new ->
        if (old != new) {
            fieldController.data = context.find { it.id == data.first }
            valueController.data = "${data.second}"
            onUpdate(old, new, this@FieldChangeController)
            refresh()
        }
    }

    override var context: List<Field> by observable(fields) { _, old, new ->
        if (old != new) {
            fieldController.context = new
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            fieldController.readOnly = new
            valueController.readOnly = new
        }
    }

    val fieldController = FieldSelectController(context.find { it.id == data.first }, context) { old, new, _ ->
        if (old != new && new != null) {
            data = new.id to data.second
        }
    }

    val valueController = editableFloatController(data.second) { old, new, _ ->
        if (old != new) {
            data = data.first to new
        }
    }

    override val container = Column(fieldController, valueController, size = ColumnSize.Full)


    override fun refresh() {
        fieldController.refresh()
        valueController.refresh()
    }

}

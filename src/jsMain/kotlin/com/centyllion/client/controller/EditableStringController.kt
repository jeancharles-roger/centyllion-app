package com.centyllion.client.controller

import bulma.*
import kotlin.properties.Delegates.observable

/**
 * Editable string controller.
 */
class EditableStringController(
    data: String = "", placeHolder: String = "",
    val onUpdate: (old: String, new: String, controller: EditableStringController) -> Unit = { _, _, _ -> }
) : Controller<String, Field> {

    override var data by observable(data) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@EditableStringController)
            refresh()
        }
    }

    val validateButton = Control(iconButton(Icon("check"), ElementColor.Success) {
        this.data = valueInput.value
        edit(false)
    })

    val cancelButton = Control(iconButton(Icon("times"), ElementColor.Danger, rounded = true) {
        edit(false)
    })

    val valueInput = Input(value = data, placeholder = placeHolder).apply {
        readonly = true
        static = true
        root.onclick = {
            edit(true)
            Unit
        }
    }

    val penIcon = Icon("pen")

    val inputControl = Control(valueInput, expanded = true, rightIcon = penIcon)

    fun edit(editable: Boolean) {
        valueInput.static = !editable
        valueInput.readonly = !editable
        container.addons = editable
        if (editable) {
            container.body = listOf(inputControl, validateButton, cancelButton)
            inputControl.rightIcon = null
        } else {
            container.body = listOf(inputControl)
            inputControl.rightIcon = penIcon
        }
    }

    override val container: Field = Field(inputControl)

    override fun refresh() {
        valueInput.value = data
    }

}


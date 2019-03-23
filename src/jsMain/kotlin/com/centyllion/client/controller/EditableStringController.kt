package com.centyllion.client.controller

import bulma.*
import kotlin.properties.Delegates.observable

/**
 * Editable string controller.
 */
class EditableStringController(
    initialData: String = "", placeHolder: String = "",
    val onUpdate: (old: String, new: String, controller: EditableStringController) -> Unit = { _, _, _ -> }
) : Controller<String, Field> {

    override var data by observable(initialData) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@EditableStringController)
            refresh()
        }
    }

    val validateButton = Control(iconButton(Icon("check"), ElementColor.Success) {
        this.data = input.value
        edit(false)
    })

    val cancelButton = Control(iconButton(Icon("times"), ElementColor.Danger, rounded = true) {
        input.value = this.data
        edit(false)
    })

    val input = Input(value = data, placeholder = placeHolder).apply {
        readonly = true
        static = true
        root.onclick = {
            edit(true)
            Unit
        }
    }

    val penIcon = Icon("pen")

    val inputControl = Control(input, expanded = true, rightIcon = penIcon)

    fun edit(editable: Boolean) {
        input.static = !editable
        input.readonly = !editable
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
        input.value = data
    }

}


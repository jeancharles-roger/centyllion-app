package com.centyllion.client.controller

import bulma.*
import kotlin.properties.Delegates.observable

/**
 * Editable string controller.
 */
class EditableStringController(
    initialData: String = "", placeHolder: String = "",
    var isValid: (value: String) -> Boolean = { true },
    var onUpdate: (old: String, new: String, controller: EditableStringController) -> Unit = { _, _, _ -> }
) : Controller<String, Field> {

    override var data by observable(initialData) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@EditableStringController)
            refresh()
        }
    }

    val okButton: Button = iconButton(Icon("check"), ElementColor.Success) {
        this.data = input.value
        edit(false)
    }

    val okControl = Control(okButton)

    val cancelButton: Button = iconButton(Icon("times"), ElementColor.Danger, rounded = true) {
        input.value = this.data
        edit(false)
    }

    val cancelControl = Control(cancelButton)

    val input = Input(value = data, placeholder = placeHolder, readonly = true, static = true).apply {
        root.onclick = {
            edit(true)
            Unit
        }
        onChange = { _, value ->
            isValid(value).let {
                color = if (it) ElementColor.None else ElementColor.Danger
                okButton.disabled = !it
            }
        }
    }

    val penIcon = Icon("pen")

    val inputControl = Control(input, expanded = true, rightIcon = penIcon)

    fun edit(editable: Boolean) {
        input.static = !editable
        input.readonly = !editable
        container.addons = editable
        if (editable) {
            container.body = listOf(inputControl, okControl, cancelControl)
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

fun editableDoubleController(
    initialData: Double = 0.0, placeHolder: String = "",
    onUpdate: (old: Double, new: Double, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData.toString(), placeHolder, { it.toDoubleOrNull() != null }, { old, new, controller ->
        onUpdate(old.toDouble(), new.toDouble(), controller)
    }
)

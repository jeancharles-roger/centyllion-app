package com.centyllion.client.controller

import bulma.*
import kotlin.properties.Delegates.observable

/**
 * Editable string controller.
 */
class EditableStringController(
    initialData: String = "", placeHolder: String = "", disabled: Boolean = false,
    var isValid: (value: String) -> Boolean = { true },
    var onUpdate: (old: String, new: String, controller: EditableStringController) -> Unit = { _, _, _ -> }
) : NoContextController<String, Field>() {

    override var data by observable(initialData) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@EditableStringController)
            refresh()
        }
    }

    var disabled: Boolean by observable(disabled) { _, old, new ->
        if (old != new) edit(false)
    }

    val okButton: Button = iconButton(Icon("check"), ElementColor.Success) { validate() }
    val cancelButton: Button = iconButton(Icon("times"), ElementColor.Danger, rounded = true) { cancel() }

    val okControl = Control(okButton)
    val cancelControl = Control(cancelButton)

    val input = Input(value = data, placeholder = placeHolder, readonly = true, static = true).apply {
        root.onclick = {
            if (!this@EditableStringController.disabled) edit(true)
            Unit
        }
        root.onkeyup = {
            if (!readonly) {
                when (it.key) {
                    "Enter" -> validate()
                    "Esc", "Escape" -> cancel()
                }
            }
        }
        onChange = { _, value ->
            isValid(value).let {
                color = if (it) ElementColor.None else ElementColor.Danger
                okButton.disabled = !it
            }
        }
        onFocus = { if (!it) validate() }
    }

    val penIcon = Icon("pen")

    val inputControl = Control(input, expanded = true, rightIcon = if (disabled) null else penIcon)

    fun edit(editable: Boolean) {
        input.static = !editable
        input.readonly = !editable
        container.addons = editable
        if (editable) {
            container.body = listOfNotNull(inputControl, okControl, cancelControl)
            inputControl.rightIcon = null
            input.root.focus()
        } else {
            container.body = listOfNotNull(inputControl)
            inputControl.rightIcon = if (this.disabled) null else penIcon
        }
    }

    override val container: Field = Field(inputControl)

    override fun refresh() {
        input.value = data
    }

    fun validate() {
        this.data = input.value
        edit(false)
    }

    fun cancel() {
        input.value = this.data
        edit(false)
    }

}

fun editableDoubleController(
    initialData: Double = 0.0, placeHolder: String = "", disabled: Boolean = false,
    onUpdate: (old: Double, new: Double, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData.toString(), placeHolder, disabled, { it.toDoubleOrNull() != null }, { old, new, controller ->
        onUpdate(old.toDouble(), new.toDouble(), controller)
    }
)

fun editableIntController(
    initialData: Int = 0, placeHolder: String = "", disabled: Boolean = false,
    onUpdate: (old: Int, new: Int, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData.toString(), placeHolder, disabled, { it.toIntOrNull() != null }, { old, new, controller ->
        onUpdate(old.toInt(), new.toInt(), controller)
    }
)

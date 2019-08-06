package com.centyllion.client.controller.utils

import bulma.Button
import bulma.Control
import bulma.ElementColor
import bulma.Field
import bulma.Icon
import bulma.Input
import bulma.NoContextController
import bulma.TextArea
import bulma.TextView
import bulma.iconButton
import kotlin.properties.Delegates.observable

/**
 * Editable string controller.
 */
class EditableStringController(
    initialData: String = "", placeHolder: String = "", readOnly: Boolean = false,
    val input: TextView = Input(value = initialData, placeholder = placeHolder, readonly = readOnly, static = true),
    validateOnEnter: Boolean = true, var isValid: (value: String) -> Boolean = { true },
    var onUpdate: (old: String, new: String, controller: EditableStringController) -> Unit =
        { _, _, _ -> }
) : NoContextController<String, Field>() {

    override var data by observable(initialData) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@EditableStringController)
            refresh()
        }
    }

    override var readOnly: Boolean by observable(readOnly) { _, old, new ->
        if (old != new) edit(false)
    }

    val okButton: Button = iconButton(Icon("check"), ElementColor.Success) { validate() }
    val cancelButton: Button = iconButton(Icon("times"), ElementColor.Danger, rounded = true) { cancel() }

    val okControl = Control(okButton)
    val cancelControl = Control(cancelButton)

    val penIcon = Icon("pen")


    val inputControl = Control(this.input, expanded = true, rightIcon = if (readOnly) null else penIcon)

    fun edit(start: Boolean) {
        // don't edit if readonly
        if (start && readOnly) return

        input.static = !start
        input.readonly = !start
        container.addons = start
        if (start) {
            container.body = listOfNotNull(inputControl, okControl, cancelControl)
            inputControl.rightIcon = null
            input.root.focus()
        } else {
            container.body = listOfNotNull(inputControl)
            inputControl.rightIcon = if (this.readOnly) null else penIcon
        }
    }

    override val container: Field = Field(inputControl)

    init {
        input.apply {
            root.onclick = { edit(true) }
            root.onkeyup = {
                if (!readonly) {
                    when (it.key) {
                        "Enter" -> if (validateOnEnter) validate()
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
            onFocus = { if (it) edit(true) else validate() }
        }

    }

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

fun editableFloatController(
    initialData: Float = 0f,
    placeHolder: String = "",
    minValue: Float = Float.MIN_VALUE,
    maxValue: Float = Float.MAX_VALUE,
    onUpdate: (old: Float, new: Float, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData.toString(), placeHolder, false,
    isValid = { string -> isNumberIn(string, String::toFloatOrNull, minValue, maxValue) },
    onUpdate = { old, new, controller -> onUpdate(old.toFloat(), new.toFloat(), controller) }
)

fun editableDoubleController(
    initialData: Double = 0.0,
    placeHolder: String = "",
    minValue: Double = Double.MIN_VALUE,
    maxValue: Double = Double.MAX_VALUE,
    onUpdate: (old: Double, new: Double, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData.toString(), placeHolder, false,
    isValid = { string -> isNumberIn(string, String::toDoubleOrNull, minValue, maxValue) },
    onUpdate = { old, new, controller -> onUpdate(old.toDouble(), new.toDouble(), controller) }
)

fun editableIntController(
    initialData: Int = 0, placeHolder: String = "", minValue: Int = Int.MIN_VALUE, maxValue: Int = Int.MAX_VALUE,
    onUpdate: (old: Int, new: Int, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData.toString(), placeHolder, false,
    isValid = { string -> isNumberIn(string, String::toIntOrNull, minValue, maxValue) },
    onUpdate = { old, new, controller -> onUpdate(old.toInt(), new.toInt(), controller) }
)

fun multilineStringController(
    initialData: String = "", placeHolder: String = "", disabled: Boolean = false,
    isValid: (value: String) -> Boolean = { true },
    onUpdate: (old: String, new: String, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData, placeHolder, disabled,
    TextArea(value = initialData, placeholder = placeHolder, readonly = true, static = true, rows = "4"),
    validateOnEnter = false, isValid = isValid, onUpdate = onUpdate
)

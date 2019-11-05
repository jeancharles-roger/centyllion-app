package com.centyllion.client.controller.utils

import bulma.Button
import bulma.Control
import bulma.ControlElement
import bulma.ElementColor
import bulma.Field
import bulma.Icon
import bulma.Input
import bulma.NoContextController
import bulma.TextArea
import bulma.TextView
import bulma.iconButton
import com.centyllion.i18n.Locale
import markdownit.MarkdownIt
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.properties.Delegates.observable

/**
 * Editable string controller.
 */
class EditableStringController(
    initialData: String = "", placeHolder: String = "", readOnly: Boolean = false, columns: Int? = null,
    val input: TextView = Input(
        value = initialData,
        placeholder = placeHolder,
        readonly = readOnly,
        static = true,
        columns = columns
    ),
    validateOnEnter: Boolean = true, var isValid: (value: String) -> String? = { null },
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

    private var message: String? by observable(isValid(initialData)) { _, old, new ->
        if (old != new) {
            if (new != null) {
                // there is a error
                input.color = ElementColor.Danger
                okButton.disabled = true
                inputControl.root.classList.add("tooltip")
                inputControl.root.classList.add("is-tooltip-active")
                inputControl.root.classList.add("is-tooltip-bottom")
                inputControl.root.classList.add("is-tooltip-danger")
                inputControl.root.setAttribute("data-tooltip", new)
            } else {
                // input is valid
                input.color = ElementColor.None
                okButton.disabled = false
                inputControl.root.classList.remove("tooltip")
                inputControl.root.classList.remove("is-tooltip-active")
                inputControl.root.classList.remove("is-tooltip-bottom")
                inputControl.root.classList.remove("is-tooltip-danger")
                inputControl.root.removeAttribute("data-tooltip")
            }
        }
    }

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
            inputControl.rightIcon = if (readOnly) null else penIcon
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
            onChange = { _, value -> message = isValid(value) }
            onFocus = { if (it) edit(true) else if (message == null) validate() else cancel() }
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

fun <T : Comparable<T>> isNumberIn(locale: Locale, placeholder: String, number: T?, min: T, max: T) = when {
    number == null -> locale.i18n("%0 must be a number", placeholder)
    number < min || number > max -> locale.i18n("%0 must be between %1 and %2", placeholder, min, max)
    else -> null
}

fun editableFloatController(
    locale: Locale, initialData: Float = 0f, placeHolder: String = "",
    minValue: Float = Float.NEGATIVE_INFINITY, maxValue: Float = Float.POSITIVE_INFINITY,
    onUpdate: (old: Float, new: Float, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData.toString(), placeHolder, false,
    isValid = { isNumberIn(locale, placeHolder, it.toFloatOrNull(), minValue, maxValue) },
    onUpdate = { old, new, controller -> onUpdate(old.toFloat(), new.toFloat(), controller) }
)

fun editableDoubleController(
    locale: Locale, initialData: Double = 0.0, placeHolder: String = "",
    minValue: Double = Double.NEGATIVE_INFINITY, maxValue: Double = Double.POSITIVE_INFINITY,
    onUpdate: (old: Double, new: Double, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData.toString(), placeHolder, false,
    isValid = { isNumberIn(locale, placeHolder, it.toDoubleOrNull(), minValue, maxValue) },
    onUpdate = { old, new, controller -> onUpdate(old.toDouble(), new.toDouble(), controller) }
)

fun editableIntController(
    locale: Locale, initialData: Int = 0, placeHolder: String = "",
    minValue: Int = Int.MIN_VALUE, maxValue: Int = Int.MAX_VALUE,
    onUpdate: (old: Int, new: Int, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData.toString(), placeHolder, false,
    isValid = { isNumberIn(locale, placeHolder, it.toIntOrNull(), minValue, maxValue) },
    onUpdate = { old, new, controller -> onUpdate(old.toInt(), new.toInt(), controller) }
)

fun multiLineStringController(
    initialData: String = "", placeHolder: String = "", readOnly: Boolean = false,
    rows: Int = 4, columns: Int? = null,
    isValid: (value: String) -> String? = { null },
    onUpdate: (old: String, new: String, controller: EditableStringController) -> Unit = { _, _, _ -> }
) = EditableStringController(
    initialData, placeHolder, readOnly, columns,
    TextArea(value = initialData, placeholder = placeHolder, readonly = true, static = true, rows = "$rows"),
    validateOnEnter = false, isValid = isValid, onUpdate = onUpdate
)

class EditableMarkdownController(
    initialData: String = "", val placeHolder: String = "", readOnly: Boolean = false, rows: Int = 4,
    var onUpdate: (old: String, new: String, controller: EditableMarkdownController) -> Unit =
        { _, _, _ -> }
) : NoContextController<String, Field>() {

    val renderer = MarkdownIt()

    override var data by observable(initialData) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@EditableMarkdownController)
            refresh()
        }
    }

    override var readOnly: Boolean by observable(readOnly) { _, old, new ->
        if (old != new) {
            htmlControl.rightIcon = if (new) null else penIcon
            edit(false)
        }
    }

    val okButton: Button = iconButton(Icon("check"), ElementColor.Success) { validate() }
    val cancelButton: Button = iconButton(Icon("times"), ElementColor.Danger, rounded = true) { cancel() }

    val okControl = Control(okButton)
    val cancelControl = Control(cancelButton)


    val input = TextArea(value = initialData, placeholder = placeHolder, readonly = true, static = true, rows = "$rows")

    val inputControl = Control(this.input, expanded = true)

    val html = object : ControlElement {
        override val root = document.createElement("div") as HTMLDivElement

        init {
            root.innerHTML = transform(initialData)
        }
    }

    val penIcon = Icon("pen")
    val htmlControl = Control(html, rightIcon = if (readOnly) null else penIcon)

    override val container: Field = Field(htmlControl)

    init {
        html.root.onclick = { edit(true) }
        input.apply {
            root.onkeyup = {
                if (!readonly) {
                    when (it.key) {
                        "Esc", "Escape" -> cancel()
                    }
                }
            }
            //onChange = { _, value ->  }
            onFocus = { if (it) edit(true) else validate() }
        }

    }

    fun edit(start: Boolean) {
        // don't edit if readonly
        if (start && readOnly) return

        input.static = !start
        input.readonly = !start
        container.addons = start
        if (start) {
            input.value = this.data
            container.body = listOf(inputControl, okControl, cancelControl)
            inputControl.rightIcon = null
            input.root.focus()
        } else {
            html.root.innerHTML = transform(data)
            container.body = listOf(htmlControl)
        }
    }

    override fun refresh() {
        input.value = data
        html.root.innerHTML = transform(data)
    }

    fun validate() {
        this.data = input.value
        edit(false)
    }

    fun cancel() {
        edit(false)
    }

    fun transform(source: String) =
        if (source.isNotBlank()) renderer.render(source)
        else "<div class='has-text-grey-lighter'>$placeHolder</div>"
}

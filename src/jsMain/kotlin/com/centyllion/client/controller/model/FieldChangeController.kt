package com.centyllion.client.controller.model

import bulma.*
import bulma.extension.Slider
import com.centyllion.client.toFixed
import com.centyllion.model.Field
import kotlin.properties.Delegates.observable

class FieldChangeController(
    value: Pair<Int, Float>, fields: List<Field>, min: Float = -1f, max: Float = 1f,
    var isValid: (id: Int, value: Float) -> String? = { _, _ -> null },
    var onUpdate: (old: Pair<Int, Float>, new: Pair<Int, Float>, controller: FieldChangeController) -> Unit = { _, _, _ -> }
) : Controller<Pair<Int, Float>, List<Field>, Column> {

    override var data: Pair<Int, Float> by observable(value) { _, old, new ->
        if (old != new) {
            validate()
            onUpdate(old, new, this@FieldChangeController)
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
            valueSlider.disabled = new
        }
    }

    private var message: String? by observable<String?>(null) { _, old, new ->
        if (old != new) {
            valueSlider.color = sliderColor
            if (new != null) {
                // there is a error
                container.root.classList.add("tooltip")
                container.root.classList.add("is-tooltip-active")
                container.root.classList.add("is-tooltip-bottom")
                container.root.classList.add("is-tooltip-danger")
                container.root.setAttribute("data-tooltip", new)
            } else {
                // input is valid
                container.root.classList.remove("tooltip")
                container.root.classList.remove("is-tooltip-active")
                container.root.classList.remove("is-tooltip-bottom")
                container.root.classList.remove("is-tooltip-danger")
                container.root.removeAttribute("data-tooltip")
            }
        }
    }

    val fieldIcon = Icon("square-full").apply { root.style.color = field?.color ?: "white" }
    val fieldLabel = span(field?.label() ?: "???")

    val sliderColor get() = when {
        message != null -> ElementColor.Danger
        data.second == 0f -> ElementColor.None
        data.second < 0f -> ElementColor.Warning
        else -> ElementColor.Info
    }

    val valueSlider = Slider(
        data.second.toString(), "$min", "$max", "0.05", sliderColor, circle = true
    ) { _, value ->
        console.log("Update slider for ${field?.name} to $value")
        data = data.first to value.toFloat()
    }

    val valueLabel = Value(data.second.toFixed(2))

    override val container = Column(
        Columns(
            Column(fieldIcon, fieldLabel, size = ColumnSize.Full),
            Column(valueSlider, valueLabel, size = ColumnSize.Full),
            multiline = true
        ),
        size = ColumnSize.OneThird
    ).apply {
        root.style.paddingTop = "0.2rem"
        root.style.paddingBottom = "0.1rem"
    }

    init {
        // Sets initial value here to correct https://github.com/jeancharles-roger/centyllion/issues/185
        valueSlider.value = data.second.toString()

        validate()
    }

    override fun refresh() {
        fieldIcon.root.style.color = field?.color ?: "white"
        fieldLabel.text = field?.label() ?: "???"
        valueSlider.value = data.second.toString()
        valueSlider.color = sliderColor
        valueLabel.text = data.second.toFixed(2)
    }

    fun validate() {
        message = isValid(data.first, data.second)
    }

}


package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Control
import bulma.Controller
import bulma.ElementColor
import bulma.Help
import bulma.Icon
import bulma.Level
import bulma.Slider
import bulma.span
import com.centyllion.model.Field
import kotlin.properties.Delegates.observable
import bulma.Field as BField

class FieldChangeController(
    value: Pair<Int, Float>, fields: List<Field>, min: Float = -1f, max: Float = 1f,
    var onUpdate: (old: Pair<Int, Float>, new: Pair<Int, Float>, controller: FieldChangeController) -> Unit = { _, _, _ -> }
) : Controller<Pair<Int, Float>, List<Field>, Column> {

    override var data: Pair<Int, Float> by observable(value) { _, old, new ->
        if (old != new) {
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

    val fieldIcon = Icon("square-full").apply { root.style.color = field?.color ?: "white" }
    val fieldLabel = span(field?.label() ?: "???")

    val sliderColor get() = when {
        data.second == 0f -> ElementColor.None
        data.second < 0f -> ElementColor.Warning
        else -> ElementColor.Info
    }

    val valueSlider = Slider(
        data.second.toString(), "$min", "$max", "0.01", sliderColor, fullWidth = true
    ) { _, value -> data = data.first to value.toFloat() }

    fun help(value: Float) = when {
        value < 0f -> "-"
        value == 0f -> "0"
        else -> "+"
    }

    val valueField = BField(Control(Help(help(min))), Control(valueSlider), Control(Help(help(max))), grouped = true)

    override val container = Column(
        Level(left = listOf(fieldIcon, fieldLabel), right = listOf(valueField)), size = ColumnSize.Full
    ).apply {
        root.style.paddingTop = "0.3rem"
        root.style.paddingBottom = "0.3rem"
    }

    override fun refresh() {
        fieldIcon.root.style.color = field?.color ?: "white"
        fieldLabel.text = field?.label() ?: "???"
        valueSlider.value = data.second.toString()
        valueSlider.color = sliderColor
    }

}

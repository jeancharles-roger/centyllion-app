package com.centyllion.client.controller.model

import bulma.*
import bulma.extension.Switch
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editableFloatController
import com.centyllion.client.controller.utils.editableIntController
import com.centyllion.client.controller.utils.editorBox
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.*
import kotlin.properties.Delegates.observable
import bulma.Field as BField

class FieldEditController (
    initialData: Field, initialContext: GrainModel, val page: BulmaPage,
    var onUpdate: (old: Field, new: Field, controller: FieldEditController) -> Unit = { _, _, _ -> }
) : Controller<Field, GrainModel, Div> {

    override var data: Field by observable(initialData) { _, old, new ->
        if (old != new) {
            colorController.data = new.color
            nameController.data = new.name
            descriptionController.data = new.description
            speedController.data = "${new.speed}"
            directionController.context = null to new.color
            directionController.data = new.allowedDirection
            halfLifeController.data = "${new.halfLife}"
            formulaController.data = new.formula
            onUpdate(old, new, this@FieldEditController)
        }
        refresh()
    }

    override var context: GrainModel by observable(initialContext) { _, old, new ->
        if (old != new) {
            refresh()
            functionsTable.body = helpBody()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            colorController.readOnly = new
            invisibleCheckbox.disabled = new
            nameController.readOnly = new
            descriptionController.readOnly = new
            speedController.readOnly = new
            directionController.readOnly = new
            halfLifeController.readOnly = new
            formulaController.readOnly = new
        }
    }

    val colorController = ColorSelectController(data.color) { _, new, _ ->
        data = data.copy(color = new)
    }

    val invisibleCheckbox = Switch(
        page.i18n("Invisible"), rounded = true, outlined = true,
        size = Size.Small, color = ElementColor.Info, checked = data.invisible
    ) { _, new -> data = data.copy(invisible = new) }

    val nameController = EditableStringController(data.name, page.i18n("Name"), columns = 8) { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, page.i18n("Description"), columns = 40) { _, new, _ ->
        data = data.copy(description = new)
    }

    val speedController = editableFloatController(
        page.appContext.locale, data.speed, page.i18n("Speed"), 0f, 1f
    ) { _, new, _ -> this.data = this.data.copy(speed = new) }

    val directionController = DirectionController(data.allowedDirection, null to data.color)
    { _, new, _ -> this.data = this.data.copy(allowedDirection = new) }

    val halfLifeController = editableIntController(
        page.appContext.locale, data.halfLife, page.i18n("Half-life"), 0
    ) { _, new, _ -> this.data = this.data.copy(halfLife = new) }


    val formulaController = EditableStringController(
        initialData = data.formula,
        placeHolder = page.i18n("Formula"),
        columns = 40,
        isValid = { validateFormula(context, it) },
        onUpdate =  { _, new, _ ->
            data = data.copy(formula = new)
        }
    )
    fun helpLine(name: String, description: String, vararg items: Any) = TableRow(
        TableCell(name).apply { root.classList.add("has-text-weight-bold") },
        TableCell(page.i18n(description, *items)),
    )

    fun fieldFunction(field: Field) = TableRow(
        TableCell("field${field.id}(index)").apply { root.classList.add("has-text-weight-bold") },
        TableCell().apply {
            body = listOfNotNull(
                span(page.i18n("Previous value for field %0 at given index", field.label(true))),
                Icon("square-full").apply { root.style.color = field.color }
            )
        }
    )

    val parametersTable = Table(
        body = listOf(
            helpLine("step", "Current simulation step"),
            helpLine("x", "Slot x position"),
            helpLine("y", "Slot y position"),
            helpLine("current", "Current field value (value if no formula is provided)"),
        ),
        fullWidth = true,
        striped = true
    )


    fun helpBody() =
        listOf(helpLine("agent(index)", "Agent id at given index")) +
        context.fields
            .sortedBy { it.id }
            .map { fieldFunction(it) }

    val functionsTable = Table(body = helpBody(), fullWidth = true, striped = true)

    val help = Section(
        SubTitle(page.i18n("Formula Parameters")),
        parametersTable,
        SubTitle(page.i18n("Model functions")),
        functionsTable,
        SubTitle(page.i18n("Operators and functions")),
        Table(
            body = listOf(
                helpLine("+, -, *, /", "Mathematical operators"),
                helpLine("%", "Modulo: returns the remainder of a division, after one number is divided by another"),
                helpLine("^", "Exponentiation: a^b means a raised to the power of b"),
                helpLine("&&, ||, !", "Logical 'and', 'or', 'not' operators"),
                helpLine("==, !=", "Equality operators"),
                helpLine("<, >, <=, >=", "Comparison operators"),
                helpLine("a ? b : c", "If else ternary operator"),
                helpLine("pi, e", "PI and E constants"),
                helpLine("abs(x)", "Absolute value"),
                helpLine("avg(a1, ..., an)", "Average of n values"),
                helpLine("cos(x), sin(x), tan(x)", "Trigonometry function including arc (acos, asin, atan) and hyperbolic (cosh, sinh, tanh)"),
                helpLine("floor(x), ceil(x), round(x)", "Floor, ceil and round functions"),
                helpLine("ln(x), log(x, base)", "Logarithmic functions"),
                helpLine("min(a1, ..., an), max(a1, ..., an)", "Min and max functions"),
                helpLine("sum(a1, ..., an)", "Summation function"),
            ),
            fullWidth = true, striped = true
        )
    )

    override val container = editorBox(page.i18n("Field"), fieldIcon,
        HorizontalField(
            Label(page.i18n("Name")),
            nameController.container
        ),
        HorizontalField(
            Label(page.i18n("Display")),
            BField(
                Control(colorController.container),
                addons = true
            ),
            BField(invisibleCheckbox)
        ),
        HorizontalField(Label(page.i18n("Description")), descriptionController.container),
        HorizontalField(Label(page.i18n("Half-life")), halfLifeController.container),
        HorizontalField(Label(page.i18n("Movement")),
            BField(Control(Value(page.i18n("Speed"))), speedController.container, grouped = true),
            BField(Control(directionController)),
        ),
        HorizontalField(Label(page.i18n("Formula")), formulaController.container),
        help
    )

    override fun refresh() {
        colorController.refresh()
        nameController.refresh()
        descriptionController.refresh()
        speedController.refresh()
        directionController.refresh()
        halfLifeController.refresh()
        formulaController.refresh()
    }

}

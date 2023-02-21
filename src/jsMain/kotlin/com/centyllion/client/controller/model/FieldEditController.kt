package com.centyllion.client.controller.model

import bulma.*
import bulma.extension.Switch
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editableFloatController
import com.centyllion.client.controller.utils.editableIntController
import com.centyllion.client.controller.utils.editorBox
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.Field
import com.centyllion.model.fieldIcon
import kotlin.properties.Delegates.observable
import bulma.Field as BField

class FieldEditController (
    initialData: Field, val page: BulmaPage,
    var onUpdate: (old: Field, new: Field, controller: FieldEditController) -> Unit = { _, _, _ -> }
) : NoContextController<Field, Div>() {

    override var data: Field by observable(initialData) { _, old, new ->
        if (old != new) {
            colorController.data = new.color
            nameController.data = new.name
            descriptionController.data = new.description
            speedController.data = "${new.speed}"
            directionController.context = null to new.color
            directionController.data = new.allowedDirection
            halfLifeController.data = "${new.halfLife}"
            onUpdate(old, new, this@FieldEditController)
        }
        refresh()
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
            BField(Control(Help(page.i18n("Speed"))), speedController.container, grouped = true),
            BField(Control(directionController)),
        )
    )

    override fun refresh() {
        colorController.refresh()
        nameController.refresh()
        descriptionController.refresh()
        speedController.refresh()
        directionController.refresh()
        halfLifeController.refresh()
    }

}

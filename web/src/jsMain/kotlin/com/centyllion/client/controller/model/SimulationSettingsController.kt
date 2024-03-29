package com.centyllion.client.controller.model

import bulma.*
import bulma.extension.Switch
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editorBox
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.SimulationSettings
import com.centyllion.model.availableSimulationSizes
import org.w3c.dom.events.Event
import kotlin.properties.Delegates.observable

class SimulationSettingsController (
    initialData: SimulationSettings, val page: BulmaPage,
    var onUpdate: (old: SimulationSettings, new: SimulationSettings, controller: SimulationSettingsController) -> Unit = { _, _, _ -> }
) : NoContextController<SimulationSettings, Div>() {

    override var data: SimulationSettings by observable(initialData) { _, old, new ->
        if (old != new) {
            sizeController.selectedIndex = availableSimulationSizes.indexOf(new.size)
            colorController.data = new.backgroundColor ?: "Grey"
            showGridSwitch.checked = data.showGrid
            gridTextureUrlController.data = data.gridTextureUrl ?: ""
            onUpdate(old, new, this@SimulationSettingsController)
        }
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            sizeController.disabled = new
            colorController.readOnly = new
            showGridSwitch.disabled = new
            gridTextureUrlController.readOnly = new
        }
    }

    val sizeController = Select(
        options = availableSimulationSizes.map { Option("$it") }, rounded = true,
        selectedIndex = availableSimulationSizes.indexOf(initialData.size)
    ) { _: Event, value: List<Option> ->
        value.firstOrNull()?.value?.let {
            data = data.copy(size = it.toInt())
        }
    }


    val colorController = ColorSelectController(data.backgroundColor ?: "Grey") { _, new, _ ->
        data = data.copy(backgroundColor = if (new.isBlank()) null else new.trim())
    }

    val showGridSwitch = Switch(
        page.i18n("Show"), checked = data.showGrid, rounded = true, outlined = true
    ) { _, c -> data = data.copy(showGrid = c) }

    val gridTextureUrlController = EditableStringController(data.gridTextureUrl ?: "", "") { _, new, _ ->
        data = data.copy(gridTextureUrl = if (new.isBlank()) null else new.trim())
    }

    override val container = editorBox(
        page.i18n("Simulation"), "cogs",
        Field(
            Label(page.i18n("Size")),
            Control(sizeController)
        ),
        Field(
            Label(page.i18n("Background Color")),
            Control(colorController.container)
        ),
        Field(
            Label(page.i18n("Grid")),
            showGridSwitch
        ),
        Field(
            Label(page.i18n("Image URL")),
            gridTextureUrlController.container
        )
    )

    override fun refresh() {
        colorController.refresh()
        gridTextureUrlController.refresh()
    }
}

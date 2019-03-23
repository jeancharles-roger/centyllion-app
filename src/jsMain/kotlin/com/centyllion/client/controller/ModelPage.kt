package com.centyllion.client.controller

import KeycloakInstance
import bulma.*
import com.centyllion.model.GrainModel
import com.centyllion.model.sample.fishRespirationModel
import kotlin.properties.Delegates.observable

class ModelPageController(val instance: KeycloakInstance) : Controller<GrainModel, Columns> {

    override var data: GrainModel by observable(fishRespirationModel(true)) { _, _, _ ->
        nameController.data = data.name
        descriptionController.data = data.description
        grainsController.data = data.grains
        behavioursController.data = data.behaviours
        refresh()
    }

    val nameController = EditableStringController(data.name, "Name") { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description") { _, new, _ ->
        data = data.copy(description = new)
    }

    val grainsController = ColumnsController(data.grains) { _, grain ->
        GrainEditController().apply { data = grain }
    }

    val behavioursController = ColumnsController(data.behaviours) { _, behaviour ->
        BehaviourEditController(data).apply { data = behaviour }
    }

    override val container = Columns(
        Column(nameController, size = ColumnSize.OneThird),
        Column(descriptionController, size = ColumnSize.TwoThirds),
        Column(Label("Grains"), size = ColumnSize.OneThird),
        Column(Label("Behaviour"), size = ColumnSize.TwoThirds),
        Column(grainsController, size = ColumnSize.OneThird),
        Column(behavioursController, size = ColumnSize.TwoThirds),
        multiline = true
    )


    init {
        refresh()
    }

    override fun refresh() {
    }


}

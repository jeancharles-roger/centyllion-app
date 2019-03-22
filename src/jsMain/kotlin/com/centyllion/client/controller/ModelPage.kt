package com.centyllion.client.controller

import KeycloakInstance
import bulma.*
import com.centyllion.model.GrainModel
import com.centyllion.model.sample.fishRespirationModel
import kotlin.properties.Delegates.observable

class ModelPageController(val instance: KeycloakInstance) : Controller<GrainModel, Columns> {

    override var data: GrainModel by observable(fishRespirationModel(true)) { _, _, _ ->
        refresh()
    }

    val name = Input { _, v ->
        data = data.copy(name = v)
    }

    val description = TextArea { _, v ->
        data = data.copy(description = v)
    }

    val grainsController = ColumnsController(data.grains) { _, grain ->
        GrainEditController().apply { data = grain }
    }

    val behavioursController = ColumnsController(data.behaviours) { _, behaviour ->
        BehaviourEditController(data).apply { data = behaviour }
    }

    override val container = Columns(
        Column(Field(Label("Name"), Control(name)), size = ColumnSize.OneThird),
        Column(Field(Label("Description"), description), size = ColumnSize.TwoThirds),
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
        name.value = data.name
        description.value = data.description
    }


}

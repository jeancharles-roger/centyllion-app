package com.centyllion.client.controller

import KeycloakInstance
import bulma.*
import com.centyllion.model.GrainModel
import com.centyllion.model.sample.immunityModel
import kotlin.properties.Delegates.observable

class ModelPageController(val instance: KeycloakInstance) : Controller<GrainModel, Columns> {

    override var data: GrainModel by observable(immunityModel()) { _, _, _ ->
        refresh()
    }

    val name = Input { _, v ->
        data = data.copy(name = v)
    }

    val description = TextArea { _, v ->
        data = data.copy(description = v)
    }

    override val container = Columns(
        Column(
            div(
                simpleField(Label("Name"), Control(name)),
                simpleField(Label("Description"), description)
            ),
            size = ColumnSize.OneThird
        ),
        Column(

        )
    )


    init {
        refresh()
    }

    override fun refresh() {
        name.value = data.name
        description.value = data.description
    }


}

package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class BehaviourDisplayController(behaviour: Behaviour, model: GrainModel) : Controller<Behaviour, GrainModel, Column> {

    override var data: Behaviour by observable(behaviour) { _, old, new ->
        if (old != new) refresh()
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) refresh()
    }

    val titleLabel = Label()
    val descriptionLabel = p()

    val body = Media(
        left = data.usedGrains(context).map { Icon("circle").apply { root.style.color = it.color } },
        center = listOf(titleLabel, descriptionLabel)
    )

    override val container = Column(body, size = ColumnSize.Full)

    override fun refresh() {
        body.left = data.usedGrains(context).map { Icon("circle").apply { root.style.color = it.color } }
        titleLabel.text = data.name
        descriptionLabel.text = data.description
    }

}

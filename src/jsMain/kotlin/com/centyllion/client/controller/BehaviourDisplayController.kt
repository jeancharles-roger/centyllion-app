package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class BehaviourDisplayController(val model: GrainModel) : Controller<Behaviour, Column> {

    override var data: Behaviour by observable(Behaviour()) { _, old, new ->
        if (old != new) refresh()
    }

    val titleLabel = Label()
    val descriptionLabel = p()

    val body = Media(center = listOf(titleLabel, descriptionLabel))

    override val container = Column(body, size = ColumnSize.Full)

    override fun refresh() {
        val ids = (data.reaction + data.mainReaction)
            .flatMap { listOf(it.reactiveId, it.productId) }.filter { it >= 0 }
            .map { model.indexedGrains[it] }.filterNotNull().toSet()
        body.left = ids.map { span(classes = "dot").apply { root.style.backgroundColor = it.color } }

        titleLabel.text = data.name
        descriptionLabel.text = data.description
    }

}

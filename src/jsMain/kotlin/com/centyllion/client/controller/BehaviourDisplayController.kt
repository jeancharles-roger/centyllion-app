package com.centyllion.client.controller

import bulma.*
import bulma.Controller
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates

class BehaviourDisplayController(val model: GrainModel) : Controller<Behaviour, Media> {

    override var data: Behaviour by Delegates.observable(Behaviour()) { _, old, new ->
        if (old != new) refresh()
    }

    val titleLabel = Label()
    val descriptionLabel = p()

    override val container = Media().apply {
        center = listOf(titleLabel, descriptionLabel)
    }

    override fun refresh() {

        val ids = (data.reaction + data.mainReaction)
            .flatMap { listOf(it.reactiveId, it.productId) }.filter { it >= 0 }
            .map { model.indexedGrains[it] }.filterNotNull().toSet()
        container.left = ids.map { span(classes = "dot").apply { root.style.backgroundColor = it.color } }

        titleLabel.text = data.name
        descriptionLabel.text = data.description
    }

}

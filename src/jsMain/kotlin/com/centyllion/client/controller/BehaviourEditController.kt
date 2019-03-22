package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class BehaviourEditController(
    val model: GrainModel, val onDelete: (Behaviour) -> Unit = {}
) : Controller<Behaviour, Column> {

    override var data: Behaviour by observable(Behaviour()) { _, old, new ->
        if (old != new) refresh()
    }

    val nameController = EditableStringController(data.name, "Name") { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.name, "Description") { _, new, _ ->
        data = data.copy(description = new)
    }

    val delete = Delete { onDelete(data) }

    val body = Media(
        center = listOf(
            Columns(
                Column(nameController, size = ColumnSize.OneThird),
                Column(descriptionController, size = ColumnSize.TwoThirds)
            )
        ),
        right = listOf(delete)
    )

    override
    val container = Column(body, size = ColumnSize.Full)

    override fun refresh() {
        // dots for used grains
        val ids = (data.reaction + data.mainReaction)
            .flatMap { listOf(it.reactiveId, it.productId) }.filter { it >= 0 }
            .map { model.indexedGrains[it] }.filterNotNull().toSet()
        body.left = ids.map { span(classes = "dot").apply { root.style.backgroundColor = it.color } }

        nameController.data = data.name
        descriptionController.data = data.description
    }

}

package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class BehaviourEditController(
    initialData: Behaviour, val model: GrainModel,
    var onUpdate: (old: Behaviour, new: Behaviour, controller: BehaviourEditController) -> Unit = { _, _, _ -> },
    var onDelete: (Behaviour, controller: BehaviourEditController) -> Unit = { _, _ ->}
) : Controller<Behaviour, Column> {

    override var data: Behaviour by observable(initialData) { _, old, new ->
        if (old != new) {
            nameController.data = data.name
            descriptionController.data = data.description
            onUpdate(old, new, this@BehaviourEditController)
            refresh()
        }
    }

    val nameController = EditableStringController(data.name, "Name") { _, new, _ ->
        this.data = this.data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description") { _, new, _ ->
        this.data = this.data.copy(description = new)
    }

    val probatilityController = editableDoubleController(data.probability, "Probability") { _, new, _ ->
        this.data = this.data.copy(probability = new)
    }

    val delete = Delete { onDelete(this.data, this@BehaviourEditController) }

    val body = Media(
        center = listOf(
            Columns(
                Column(nameController, size = ColumnSize.S4),
                Column(descriptionController, size = ColumnSize.None),
                Column(probatilityController, size = ColumnSize.S3)
            )
        ),
        right = listOf(delete)
    )

    override
    val container = Column(body, size = ColumnSize.Full)

    init {
        refresh()
    }

    override fun refresh() {
        // dots for used grains
        body.left = data.usedGrains(model).map { span(classes = "dot").apply { root.style.backgroundColor = it.color } }
    }

}
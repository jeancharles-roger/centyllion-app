package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import com.centyllion.model.Reaction
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
            probatilityController.data = "${data.probability}"
            agePredicateController.data = data.agePredicate
            mainReactiveController.data = model.indexedGrains[data.mainReactiveId]
            mainProductController.data = model.indexedGrains[data.mainProductId]
            transform.checked = data.transform
            reactionController.data = data.reaction
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

    val agePredicateController = IntPredicateController(data.agePredicate)  { _, new, _ ->
        this.data = this.data.copy(agePredicate = new)
    }

    val mainReactiveController = GrainSelectController(model.indexedGrains[data.mainReactiveId], model.grains) { _, new, _ ->
        this.data = this.data.copy(mainReactiveId = new?.id ?: -1)
    }

    val mainProductController = GrainSelectController(model.indexedGrains[data.mainProductId], model.grains) { _, new, _ ->
        this.data = this.data.copy(mainProductId = new?.id ?: -1)
    }

    val transform = Checkbox("transform", data.transform) { _, value ->
        this.data = this.data.copy(transform = value)
    }

    val addReactionButton = iconButton(Icon("plus", Size.Small), ElementColor.Info, true, size = Size.Small) {
        val newReaction = Reaction()
        this.data = data.copy(reaction = data.reaction + newReaction)
    }

    val reactionController = ColumnsController<Reaction, ReactionEditController>(data.reaction) { index, reaction, previous ->
        val controller = previous ?: ReactionEditController(reaction, model)
        controller.onUpdate = { _, new, _ ->
            val newList = data.reaction.toMutableList()
            newList[index] = new
            data = data.copy(reaction = newList)
        }
        controller.onDelete = { _, _ ->
            val newList = data.reaction.toMutableList()
            newList.removeAt(index)
            data = data.copy(reaction = newList)
        }
        controller
    }

    val delete = Delete { onDelete(this.data, this@BehaviourEditController) }

    val body = Media(
        center = listOf(
            Columns(
                // first line
                Column(nameController, size = ColumnSize.S6),
                Column(HorizontalField(Help("Probability"), probatilityController.container), size = ColumnSize.S6),
                // second line
                Column(descriptionController, size = ColumnSize.S7),
                Column(HorizontalField(Help("Age"), agePredicateController.container), size = ColumnSize.S5),
                // third line
                Column(HorizontalField(Help("Reactive"), mainReactiveController.container), size = ColumnSize.S4),
                Column(HorizontalField(Help("Product"), mainProductController.container), size = ColumnSize.S4),
                Column(Field(Control(transform))),
                Column(addReactionButton, size = ColumnSize.S1),
                multiline = true
            ),
            reactionController
        ),
        right = listOf(delete)
    )

    override
    val container = Column(body, size = ColumnSize.Full)

    override fun refresh() { }

}

package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import com.centyllion.model.Reaction
import kotlin.properties.Delegates.observable

class BehaviourEditController(
    initialData: Behaviour, model: GrainModel,
    var onUpdate: (old: Behaviour, new: Behaviour, controller: BehaviourEditController) -> Unit = { _, _, _ -> },
    var onDelete: (Behaviour, controller: BehaviourEditController) -> Unit = { _, _ -> }
) : Controller<Behaviour, GrainModel, Column> {

    override var data: Behaviour by observable(initialData) { _, old, new ->
        if (old != new) {
            nameController.data = data.name
            descriptionController.data = data.description
            probatilityController.data = "${data.probability}"
            agePredicateController.data = data.agePredicate
            mainReactiveController.data = context.indexedGrains[data.mainReactiveId]
            mainProductController.data = context.indexedGrains[data.mainProductId]
            transform.checked = data.transform
            reactionController.data = data.reaction
            onUpdate(old, new, this@BehaviourEditController)
            refresh()
        }
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            mainReactiveController.data = context.indexedGrains[data.mainReactiveId]
            mainReactiveController.context = new.grains
            mainProductController.data = context.indexedGrains[data.mainProductId]
            mainProductController.context = new.grains
            reactionController.context = context
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

    val agePredicateController = IntPredicateController(data.agePredicate) { _, new, _ ->
        this.data = this.data.copy(agePredicate = new)
    }

    val mainReactiveController = GrainSelectController(context.indexedGrains[data.mainReactiveId], context.grains) { _, new, _ ->
        this.data = this.data.copy(mainReactiveId = new?.id ?: -1)
    }

    val mainProductController = GrainSelectController(context.indexedGrains[data.mainProductId], context.grains) { _, new, _ ->
        this.data = this.data.copy(mainProductId = new?.id ?: -1)
    }

    val transform = Checkbox("Transform", data.transform) { _, value ->
        this.data = this.data.copy(transform = value)
    }

    val addReactionButton = iconButton(Icon("plus", Size.Small), ElementColor.Info, true, size = Size.Small) {
        val newReaction = Reaction()
        this.data = data.copy(reaction = data.reaction + newReaction)
    }

    val reactionHeader = listOf(
        Column(
            Level(
                center = listOf(
                    HorizontalField(Help("Reactive"), mainReactiveController.container),
                    HorizontalField(Help("Product"), mainProductController.container),
                    Field(Control(transform))
                ),
                right = listOf(addReactionButton)
            ),
            size = ColumnSize.Full
        )
    )

    val reactionController = ColumnsController<Reaction, GrainModel, ReactionEditController>(
        data.reaction, context, reactionHeader
    ) { index, reaction, previous ->
        val controller = previous ?: ReactionEditController(reaction, context)
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
                multiline = true
            ),
            reactionController
        ),
        right = listOf(delete)
    )

    override
    val container = Column(body, size = ColumnSize.Full)

    override fun refresh() {
        addReactionButton.disabled = data.reaction.size >= 2

        nameController.refresh()
        descriptionController.refresh()
        probatilityController.refresh()
        agePredicateController.refresh()
        mainReactiveController.refresh()
        mainProductController.refresh()
        reactionController.refresh()
    }

}

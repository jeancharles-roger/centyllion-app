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
            probabilityController.data = "${data.probability}"
            agePredicateController.data = data.agePredicate
            mainReactiveController.data = context.indexedGrains[data.mainReactiveId]
            mainProductController.data = context.indexedGrains[data.mainProductId]
            sourceReactiveController.data = data.sourceReactive
            sourceReactiveController.context = data to context
            reactionController.data = data.reaction
            reactionController.context = data to context
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
            sourceReactiveController.context = data to context
            reactionController.context = data to context
            refresh()
        }
    }

    val nameController = EditableStringController(data.name, "Name")
    { _, new, _ ->
        this.data = this.data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description")
    { _, new, _ ->
        this.data = this.data.copy(description = new)
    }

    val probabilityController = editableDoubleController(data.probability, "Probability")
    { _, new, _ ->
        this.data = this.data.copy(probability = new)
    }

    val agePredicateController = IntPredicateController(data.agePredicate)
    { _, new, _ ->
        this.data = this.data.copy(agePredicate = new)
    }

    val mainReactiveController = GrainSelectController(context.indexedGrains[data.mainReactiveId], context.grains)
    { _, new, _ ->
        this.data = this.data.copy(mainReactiveId = new?.id ?: -1)
    }

    val mainProductController = GrainSelectController(context.indexedGrains[data.mainProductId], context.grains)
    { _, new, _ ->
        this.data = this.data.copy(mainProductId = new?.id ?: -1)
    }

    val sourceReactiveController = SourceReactiveSelectController(data.sourceReactive, data, context)
    { _, new, _ ->
        this.data = this.data.copy(sourceReactive = new)
    }

    val addReactionButton = iconButton(Icon("plus", Size.Small), ElementColor.Info, true, size = Size.Small) {
        val newReaction = Reaction()
        this.data = data.copy(reaction = data.reaction + newReaction)
    }

    val reactionHeader = listOf(
        Column(
            Level(
                left = listOf(HorizontalField(Help("Reactive"), mainReactiveController.container)),
                right = listOf(
                    HorizontalField(Help("Product"), mainProductController.container),
                    sourceReactiveController.container,
                    addReactionButton
                )
            ),
            size = ColumnSize.Full
        )
    )

    val reactionController =
        columnsController<Reaction, Pair<Behaviour, GrainModel>, ReactionEditController>(
            data.reaction, data to context, reactionHeader
        ) { index, reaction, previous ->
            val controller = previous ?: ReactionEditController(reaction, data, context)
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
                Column(HorizontalField(Help("Probability"), probabilityController.container), size = ColumnSize.S6),
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
        probabilityController.refresh()
        agePredicateController.refresh()
        mainReactiveController.refresh()
        mainProductController.refresh()
        reactionController.refresh()
    }

}

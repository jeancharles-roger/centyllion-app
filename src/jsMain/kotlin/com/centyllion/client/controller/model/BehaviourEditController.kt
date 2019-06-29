package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Controller
import bulma.Delete
import bulma.ElementColor
import bulma.Help
import bulma.HorizontalField
import bulma.Icon
import bulma.Level
import bulma.Media
import bulma.Size
import bulma.columnsController
import bulma.iconButton
import bulma.wrap
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editableDoubleController
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import com.centyllion.model.Reaction
import com.centyllion.model.extendedDirections
import com.centyllion.model.firstDirections
import kotlin.properties.Delegates.observable

class BehaviourEditController(
    initialData: Behaviour, model: GrainModel,
    var onUpdate: (old: Behaviour, new: Behaviour, controller: BehaviourEditController) -> Unit = { _, _, _ -> },
    var onDelete: (Behaviour, controller: BehaviourEditController) -> Unit = { _, _ -> }
) : Controller<Behaviour, GrainModel, Media> {

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

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            addReactionButton.invisible = new
            nameController.readOnly = new
            descriptionController.readOnly = new
            probabilityController.readOnly = new
            agePredicateController.readOnly = new
            mainReactiveController.readOnly = new
            mainProductController.readOnly = new
            sourceReactiveController.readOnly = new
            reactionController.readOnly = new
            container.right = if (new) emptyList() else listOf(delete)
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

    val probabilityController = editableDoubleController(data.probability, "Speed")
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

    val dummyFirstDirectionController = DirectionSetEditController(firstDirections, emptySet()).apply { invisible = true }
    val dummyExtendedDirectionController = DirectionSetEditController(extendedDirections, emptySet()).apply { invisible = true }

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
        // Title header
        Column(
            Level(
                left = listOf(Help("Reactive")),
                center = listOf(Help(""), Help("Product"), Help("Source")),
                right = listOf(Help("")),
                mobile = true
            ), size = ColumnSize.Full
        ),
        // Main reactive header
        Column(
            Level(
                left = listOf(mainReactiveController),
                center = listOf(
                    dummyFirstDirectionController, dummyExtendedDirectionController,
                    mainProductController, sourceReactiveController
                ),
                right = listOf(addReactionButton),
                mobile = true
            ),
            size = ColumnSize.Full
        )
    )

    val reactionController =
        columnsController(
            data.reaction, data to context, reactionHeader
        ) { reaction, previous ->
            previous ?: ReactionEditController(reaction, data, context).wrap { controller ->
                controller.onUpdate = { old, new, _ ->
                    val newList = data.reaction.toMutableList()
                    newList[data.reactionIndex(old)] = new
                    data = data.copy(reaction = newList)
                }
                controller.onDelete = { delete, _ ->
                    val newList = data.reaction.toMutableList()
                    newList.removeAt(data.reactionIndex(delete))
                    data = data.copy(reaction = newList)
                }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val delete = Delete { onDelete(this.data, this@BehaviourEditController) }

    override val container = Media(
        center = listOf(
            Columns(
                // first line
                Column(nameController, size = ColumnSize.S7),
                Column(HorizontalField(Control(Help("Speed")), probabilityController.container), size = ColumnSize.S5),
                // second line
                Column(descriptionController, size = ColumnSize.S7),
                Column(HorizontalField(Control(Help("Age")), agePredicateController.container), size = ColumnSize.S5),
                multiline = true
            ),
            reactionController
        ),
        right = listOf(delete)
    ).apply {
        root.classList.add("is-outlined")
    }

    override fun refresh() {
        addReactionButton.disabled = data.reaction.size >= 4

        nameController.refresh()
        descriptionController.refresh()
        probabilityController.refresh()
        agePredicateController.refresh()
        mainReactiveController.refresh()
        mainProductController.refresh()
        reactionController.refresh()
    }

}

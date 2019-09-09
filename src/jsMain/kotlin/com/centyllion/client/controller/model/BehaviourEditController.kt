package com.centyllion.client.controller.model

import bulma.Box
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Controller
import bulma.Div
import bulma.ElementColor
import bulma.Help
import bulma.HorizontalField
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.MultipleController
import bulma.Size
import bulma.Tag
import bulma.TileAncestor
import bulma.TileChild
import bulma.TileParent
import bulma.TileSize
import bulma.columnsController
import bulma.iconButton
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editableDoubleController
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import com.centyllion.model.Operator
import com.centyllion.model.Predicate
import com.centyllion.model.Reaction
import kotlin.properties.Delegates.observable

class BehaviourEditController(
    initialData: Behaviour, model: GrainModel,
    var onUpdate: (old: Behaviour, new: Behaviour, controller: BehaviourEditController) -> Unit =
        { _, _, _ -> }
) : Controller<Behaviour, GrainModel, Div> {

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
            reactionsController.data = data.reaction
            reactionsController.context = data to context
            fieldPredicatesController.data = data.fieldPredicates
            fieldInfluencesController.data = context.fields.map { it.id to (data.fieldInfluences[it.id] ?: 0f) }
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
            reactionsController.context = data to context
            fieldPredicatesController.context = context.fields
            fieldInfluencesController.context = context.fields
            fieldInfluencesController.data = context.fields.map { it.id to (data.fieldInfluences[it.id] ?: 0f) }
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            addReactionButton.invisible = new
            addFieldPredicateButton.invisible = new
            nameController.readOnly = new
            descriptionController.readOnly = new
            probabilityController.readOnly = new
            agePredicateController.readOnly = new
            mainReactiveController.readOnly = new
            mainProductController.readOnly = new
            sourceReactiveController.readOnly = new
            reactionsController.readOnly = new
            reactionEditTile.hidden = new
            fieldPredicatesController.readOnly = new
            fieldInfluencesController.readOnly = new
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

    val probabilityController = editableDoubleController(data.probability, "Speed", 0.0, 1.0)
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
        // Header
        TileParent(
            TileChild(Help("Reactive")),
            TileChild(Help("Directions")),
            TileChild(Help("Product")),
            TileChild(Help("Source")),
            TileChild()
        ),
        // Main reactive
        TileParent(
            TileChild(mainReactiveController),
            TileChild(),
            TileChild(mainProductController),
            TileChild(sourceReactiveController),
            TileChild(addReactionButton)
        )
    )

    val reactionEditTile = TileParent(size = TileSize.S1, vertical = true)

    val reactionsController = MultipleController<Reaction, Pair<Behaviour, GrainModel>, TileAncestor, TileParent, ReactionEditController>(
        data.reaction, data to context, reactionHeader, emptyList(),
        TileAncestor( *Array(5) { if (it == 4) reactionEditTile else TileParent(vertical = true) } ), null,
        { reaction, previous ->
            previous ?: ReactionEditController(reaction, data, context).also {
                it.onUpdate = { old, new, _ ->
                    data = data.updateReaction(old, new)
                }
                it.onDelete = { delete, _ ->
                    data = data.dropReaction(delete)
                }
            }
        },
        { parent, items ->
            parent.body.forEachIndexed { index, tileInner ->
                (tileInner as TileParent).body = items.map { it.body[index] }
            }
        }
    )

    val addFieldPredicateButton = iconButton(Icon("plus", Size.Small), ElementColor.Info, true, size = Size.Small) {
        val predicate = context.fields.first().id to Predicate(Operator.GreaterThan, 0f)
        this.data = data.copy(fieldPredicates = data.fieldPredicates + predicate)
    }

    val fieldPredicatesController =
        columnsController(data.fieldPredicates, context.fields) { pair, previous ->
            previous ?: FieldPredicateController(pair, context.fields,
            { old, new, _ -> if (old != new) this.data = data.updateFieldPredicate(old, new) },
            { delete, _ -> this.data = data.dropFieldPredicate(delete) })
        }

    val fieldInfluencesController =
        columnsController(context.fields.map { it.id to (data.fieldInfluences[it.id] ?: 0f) }, context.fields) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateFieldInfluence(new.first, new.second)
                }
            }
        }

    val fieldsConfiguration = Columns(
        Column(
            Level(
                left= listOf(Label("Field thresholds")),
                right = listOf(addFieldPredicateButton)
            ),
            fieldPredicatesController, size = ColumnSize.S7
        ),
        Column(Label("Field influences"), fieldInfluencesController, size = ColumnSize.S5)
    ).apply {
        hidden = context.fields.isEmpty()
    }

    override val container = Div(
        Tag("Behaviour", ElementColor.Primary, Size.Large),
        Box(
            HorizontalField(Label("Name"), nameController.container),
            HorizontalField(Label("Description"), descriptionController.container),
            HorizontalField(Label("Speed"), probabilityController.container),
            HorizontalField(Label("When age"), agePredicateController.container),
            Label("Reactions"), reactionsController,
            fieldsConfiguration
        )
    )

    override fun refresh() {
        addReactionButton.disabled = data.reaction.size >= 4

        nameController.refresh()
        descriptionController.refresh()
        probabilityController.refresh()
        agePredicateController.refresh()
        mainReactiveController.refresh()
        mainProductController.refresh()
        reactionsController.refresh()

        fieldPredicatesController.refresh()
        fieldInfluencesController.refresh()
        fieldsConfiguration.hidden = context.fields.isEmpty()
    }

}

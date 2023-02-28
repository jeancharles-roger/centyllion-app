package com.centyllion.client.controller.model

import bulma.*
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.createHr
import com.centyllion.client.controller.utils.editableDoubleController
import com.centyllion.client.controller.utils.editorBox
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.*
import com.centyllion.model.Field
import kotlin.properties.Delegates.observable

class BehaviourEditController(
    initialData: Behaviour, model: GrainModel, val page: BulmaPage,
    var onUpdate: (old: Behaviour, new: Behaviour, controller: BehaviourEditController) -> Unit =
        { _, _, _ -> }
) : Controller<Behaviour, GrainModel, Div> {

    override var data: Behaviour by observable(initialData) { _, old, new ->
        if (old != new) {
            nameController.data = new.name
            descriptionController.data = new.description
            probabilityController.data = "${new.probability}"
            agePredicateController.data = new.agePredicate
            mainReactiveController.data = context.grainForId(new.mainReactiveId)
            mainDirection.context = null to context.grainForId(new.mainReactiveId)?.color
            mainProductController.data = context.grainForId(new.mainProductId)
            sourceReactiveController.data = new.sourceReactive
            sourceReactiveController.context = new to context
            reactionsController.data = new.reaction
            reactionsController.context = new to context
            fieldPredicatesController.data = new.fieldPredicates
            fieldInfluencesController.data = context.fields.map { it.id to (new.fieldInfluences[it.id] ?: 0f) }
            onUpdate(old, new, this@BehaviourEditController)
            refresh()
        }
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            mainReactiveController.data = context.grainForId(data.mainReactiveId)
            mainReactiveController.context = new.grains
            mainProductController.data = context.grainForId(data.mainProductId)
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

    val nameController = EditableStringController(data.name, page.i18n("Name"))
    { _, new, _ ->
        this.data = this.data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, page.i18n("Description"))
    { _, new, _ ->
        this.data = this.data.copy(description = new)
    }

    val probabilityController = editableDoubleController(
        page.appContext.locale, data.probability, page.i18n("Speed"), 0.0, 1.0
    ) { _, new, _ ->
        this.data = this.data.copy(probability = new)
    }

    val agePredicateController = IntPredicateController(page.appContext.locale, data.agePredicate, page.i18n("Age"), 0)
    { _, new, _ ->
        this.data = this.data.copy(agePredicate = new)
    }

    val mainReactiveController = GrainSelectController(
        context.grainForId(data.mainReactiveId), context.grains, page, allowNone = false
    ) { _, new, _ -> this.data = this.data.copy(mainReactiveId = new?.id ?: -1) }

    val mainProductController = GrainSelectController(context.grainForId(data.mainProductId), context.grains, page)
    { _, new, _ ->
        this.data = this.data.copy(mainProductId = new?.id ?: -1)
    }

    val sourceReactiveController = SourceReactiveSelectController(data.sourceReactive, data, context, page)
    { _, new, _ ->
        this.data = this.data.copy(sourceReactive = new)
    }

    val addReactionButton = iconButton(Icon("plus", Size.Small), ElementColor.Primary, true, size = Size.Small) {
        val newReaction = Reaction()
        this.data = data.copy(reaction = data.reaction + newReaction)
    }

    val mainDirection = DirectionController(
        initial = emptySet(),
        initialContext = null to context.grainForId(data.mainReactiveId)?.color,
        readOnly = true
    )

    val reactionHeader = listOf(
        // Header
        Column(
            Columns(
                Column(Help(page.i18n("Reactives")), size = ColumnSize.S4),
                Column(Help(page.i18n("Directions")), size = ColumnSize.S1),
                Column(Help(page.i18n("Products")), size = ColumnSize.S4),
                Column(Help(page.i18n("Sources")), size = ColumnSize.S2),
                Column(size = ColumnSize.S1)
            ),
            size = ColumnSize.Full
        ),
        // Main reactive
        Column(
            Columns(
                Column(mainReactiveController, size = ColumnSize.S4),
                Column(mainDirection, size = ColumnSize.S1),
                Column(mainProductController, size = ColumnSize.S4),
                Column(sourceReactiveController, size = ColumnSize.S2),
                Column(addReactionButton, size = ColumnSize.S1).apply { root.classList.add("has-text-right") }
            ),
            size = ColumnSize.Full
        )
    )

    val reactionEditTile = TileParent(size = TileSize.S1, vertical = true)


    val reactionsController = columnsController(
        initialList = data.reaction,
        initialContext = data to context,
        header = reactionHeader,
        footer = emptyList(),
        controllerBuilder = { reaction, previous ->
            previous ?: ReactionEditController(reaction, data, context, page).also {
                it.onUpdate = { old, new, _ ->
                    data = data.updateReaction(old, new)
                }
                it.onDelete = { delete, _ ->
                    data = data.dropReaction(delete)
                }
            }
        },
    )
    /*
    val reactionsController = MultipleController<Reaction, Pair<Behaviour, GrainModel>, TileAncestor, TileParent, ReactionEditController>(
        initialList = data.reaction,
        initialContext = data to context,
        header = reactionHeader,
        footer = emptyList(),
        container = TileAncestor( *Array(5) { if (it == 4) reactionEditTile else TileParent(vertical = true) } ),
        onClick = null,
        controllerBuilder = { reaction, previous ->
            previous ?: ReactionEditController(reaction, data, context, page).also {
                it.onUpdate = { old, new, _ ->
                    data = data.updateReaction(old, new)
                }
                it.onDelete = { delete, _ ->
                    data = data.dropReaction(delete)
                }
            }
        },
        updateParent = { parent, items ->
            parent.body.forEachIndexed { index, tileInner ->
                (tileInner as TileParent).body = items.map { it.body[index] }
            }
        }
    )
    */

    val addFieldPredicateButton = iconButton(Icon("plus", Size.Small), ElementColor.Primary, true, size = Size.Small) {
        val predicate = context.fields.first().id to Predicate(Operator.GreaterThan, 0f)
        this.data = data.copy(fieldPredicates = data.fieldPredicates + predicate)
    }

    val fieldPredicatesController: MultipleController<
            Pair<Int, Predicate<Float>>, List<Field>, Columns, Column, FieldPredicateController
    > = columnsController(data.fieldPredicates, context.fields) { pair, previous ->
            previous ?: FieldPredicateController(page.appContext.locale, pair, context.fields,
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

    fun fieldSeparator() = HtmlWrapper(createHr()).apply {
        hidden = context.fields.isEmpty()
    }

    val fieldsConfiguration = Columns(
        Column(
            fieldSeparator().apply { root.style.marginTop = "0.2rem" },
            size = ColumnSize.Full
        ),
        Column(
            Level(
                left= listOf(Label(page.i18n("Field thresholds"))),
                right = listOf(addFieldPredicateButton)
            ),
            fieldPredicatesController,
            size = ColumnSize.Full
        ),
        Column(
            Label(page.i18n("Field influences")),
            fieldInfluencesController,
            size = ColumnSize.Full
        ),
        multiline = true
    ).apply { hidden = context.fields.isEmpty() }

    override val container = editorBox(page.i18n("Behaviour"), behaviourIcon,
        HorizontalField(Label(page.i18n("Name")), nameController.container),
        HorizontalField(Label(page.i18n("Description")), descriptionController.container),
        HorizontalField(Label(page.i18n("Speed")), probabilityController.container),
        HorizontalField(Label(page.i18n("When age")), agePredicateController.container),
        HtmlWrapper(createHr()),
        Label(page.i18n("Reactions")), reactionsController,
        fieldsConfiguration,
    )

    override fun refresh() {
        addReactionButton.disabled = data.reaction.size >= 8

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

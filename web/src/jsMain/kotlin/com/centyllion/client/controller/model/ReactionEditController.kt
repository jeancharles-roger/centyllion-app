package com.centyllion.client.controller.model

import bulma.*
import com.centyllion.client.controller.utils.createHr
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import com.centyllion.model.Reaction
import kotlin.properties.Delegates.observable

class ReactionEditController(
    reaction: Reaction, behaviour: Behaviour, model: GrainModel, page: BulmaPage, expertMode: Boolean,
    var onUpdate: (old: Reaction, new: Reaction, controller: ReactionEditController) -> Unit = { _, _, _ -> },
    var onDelete: (Reaction, controller: ReactionEditController) -> Unit = { _, _ -> }
) : Controller<Reaction, Pair<Behaviour, GrainModel>, Column> {

    override var data: Reaction by observable(reaction) { _, old, new ->
        if (old != new) {
            reactiveController.data = context.second.grainForId(data.reactiveId)
            directionController.data = data.allowedDirection
            productController.data = context.second.grainForId(data.productId)
            sourceReactiveController.data = data.sourceReactive
            onUpdate(old, new, this@ReactionEditController)
        }
        refresh()
    }

    override var context: Pair<Behaviour, GrainModel> by observable(behaviour to model)
    { _, old, new ->
        if (old.second != new.second) {
            reactiveController.data = context.second.grainForId(data.reactiveId)
            reactiveController.context = new.second.grains
            productController.data = context.second.grainForId(data.productId)
            productController.context = new.second.grains
            refresh()
        }

        if (old != new) {
            directionController.context = directionColors()
            sourceReactiveController.context = context
        }
    }

    var expertMode: Boolean by observable(expertMode) { _, old, new ->
        if (old != new) {
            sourceReactiveController.hidden = !new
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            reactiveController.readOnly = new
            productController.readOnly = new
            sourceReactiveController.readOnly = new
            delete.hidden = new
        }
    }

    val separator = HtmlWrapper(createHr()).apply { root.style.margin = "0.2rem" }

    val reactiveController = GrainSelectController(context.second.grainForId(data.reactiveId), context.second.grains, page)
    { _, new, _ ->
        this.data = this.data.copy(reactiveId = new?.id ?: -1)
    }

    val directionController = DirectionController(
        initial = data.allowedDirection,
        initialContext = directionColors(),
        errorIfEmpty = true,
        onUpdate = { _, new, _ -> this.data = this.data.copy(allowedDirection = new) }
    )

    fun directionColors(): Pair<String?, String?> =
        context.second.grainForId(data.reactiveId)?.color to
            context.second.grainForId(context.first.mainReactiveId)?.color

    val productController = GrainSelectController(
        grain = context.second.grainForId(data.productId),
        grains = context.second.grains,
        page = page,
        onUpdate =  { _, new, _ ->
            this.data = this.data.copy(productId = new?.id ?: -1)
        }
    )


    val sourceReactiveController = SourceReactiveSelectController(
        index = data.sourceReactive,
        behaviour = context.first,
        model = context.second,
        page = page,
        onUpdate = { _, new, _ ->
            this.data = this.data.copy(sourceReactive = new)
        }
    ).apply { hidden = !expertMode }


    val delete = iconButton(Icon("times", Size.Small), ElementColor.Danger, true, size = Size.Small)
    {
        onDelete(this.data, this@ReactionEditController)
    }

    override val container = Column(
        Columns(
            Column(separator, size = ColumnSize.Full).apply { root.style.padding = "0rem" },
            Column(reactiveController, size = ColumnSize.S4),
            Column(directionController, size = ColumnSize.S1).apply { root.classList.add("has-text-centered") },
            Column(productController, size = ColumnSize.S4),
            Column(sourceReactiveController, size = ColumnSize.S2),
            Column(delete, size = ColumnSize.S1).apply { root.classList.add("has-text-right") },
            multiline = true
        ),
        size = ColumnSize.Full
    )

    override fun refresh() {
        reactiveController.refresh()
        directionController.refresh()
        productController.refresh()
    }

}

package com.centyllion.client.controller.model

import bulma.*
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.*
import kotlin.properties.Delegates.observable

class ReactionEditController(
    reaction: Reaction, behaviour: Behaviour, model: GrainModel, page: BulmaPage,
    var onUpdate: (old: Reaction, new: Reaction, controller: ReactionEditController) -> Unit = { _, _, _ -> },
    var onDelete: (Reaction, controller: ReactionEditController) -> Unit = { _, _ -> }
) : Controller<Reaction, Pair<Behaviour, GrainModel>, TileParent> {

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

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            reactiveController.readOnly = new
            firstDirectionController.readOnly = new
            extendedDirectionController.readOnly = new
            productController.readOnly = new
            sourceReactiveController.readOnly = new
            delete.hidden = new
        }
    }

    val reactiveController = GrainSelectController(context.second.grainForId(data.reactiveId), context.second.grains, page)
    { _, new, _ ->
        this.data = this.data.copy(reactiveId = new?.id ?: -1)
    }

    val directionController = DirectionController(
        initial = data.allowedDirection, initialContext = directionColors()
    )

    fun directionColors(): Pair<String?, String?> =
        context.second.grainForId(data.reactiveId)?.color to
            context.second.grainForId(context.first.mainReactiveId)?.color

    val firstDirectionController = DirectionSetEditController(firstDirections, data.allowedDirection)
    { _, new, _ ->
        this.data = this.data.copy(allowedDirection = new)
    }

    val extendedDirectionController = DirectionSetEditController(extendedDirections, data.allowedDirection)
    { _, new, _ ->
        this.data = this.data.copy(allowedDirection = new)
    }

    val productController = GrainSelectController(context.second.grainForId(data.productId), context.second.grains, page)
    { _, new, _ ->
        this.data = this.data.copy(productId = new?.id ?: -1)
    }

    val sourceReactiveController = SourceReactiveSelectController(data.sourceReactive, context.first, context.second, page)
    { _, new, _ ->
        this.data = this.data.copy(sourceReactive = new)
    }

    val delete = iconButton(Icon("times", Size.Small), ElementColor.Danger, true, size = Size.Small)
    {
        onDelete(this.data, this@ReactionEditController)
    }

    override val container  = TileParent(
        TileChild(reactiveController),
        TileChild(directionController),
        TileChild(productController),
        TileChild(sourceReactiveController),
        TileChild(delete).apply { root.classList.add("has-text-right") }
    )

    override fun refresh() {
        reactiveController.refresh()
        firstDirectionController.refresh()
        extendedDirectionController.refresh()
        productController.refresh()

        firstDirectionController.error = data.allowedDirection.isEmpty()
        extendedDirectionController.error = data.allowedDirection.isEmpty()
    }

}

package com.centyllion.client.controller.model

import bulma.Box
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Field
import bulma.Help
import bulma.Label
import bulma.Level
import bulma.NoContextController
import bulma.Slider
import com.centyllion.client.AppContext
import com.centyllion.client.controller.utils.DeleteCallbackProperty
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editableDoubleController
import com.centyllion.model.Asset3d
import kotlin.properties.Delegates.observable

class Asset3dEditController(
    initialData: Asset3d, readOnly: Boolean = false, appContext: AppContext,
    var onUpdate: (old: Asset3d, new: Asset3d, controller: Asset3dEditController) -> Unit = { _, _, _ -> },
    onDelete: ((Asset3d) -> Unit)? = null
) : NoContextController<Asset3d, Box>() {

    override var data: Asset3d by observable(initialData) { _, old, new ->
        if (old != new) {
            urlController.data = data.url
            assetSelectController.data = data.url
            onUpdate(old, new, this@Asset3dEditController)
            refresh()
        }
    }

    override var readOnly: Boolean by observable(readOnly) { _, old, new ->
        if (old != new) {
            assetSelectController.readOnly = new
            urlController.readOnly = new
            opacitySlider.disabled = new
            xController.readOnly = new
            yController.readOnly = new
            zController.readOnly = new
            xScaleController.readOnly = new
            yScaleController.readOnly = new
            zScaleController.readOnly = new
            xRotationController.readOnly = new
            yRotationController.readOnly = new
            zRotationController.readOnly = new
            deleteCallbackProperty.readOnly = new
        }
    }

    val assetSelectController = Asset3dSelectController(data.url, appContext) { old, new, _ ->
        if (old != new) this.data = this.data.copy(url = new)
    }

    val urlController = EditableStringController(data.url, "Url")
    { _, new, _ ->
        this.data = this.data.copy(url = new)
    }

    val opacitySlider = Slider(
        data.opacity.toString(), "0.0", "1.0", "0.01", fullWidth = true
    ) { _, value -> data = data.copy(opacity = value.toDouble()) }

    val opacityField = Field(
        Control(Help("transparent")), Control(opacitySlider), Control(Help("visible")),
        grouped = true
    )

    val xController = editableDoubleController(data.x)
    { _, new, _ -> data = data.copy(x = new) }

    val yController = editableDoubleController(data.y)
    { _, new, _ -> data = data.copy(y = new) }

    val zController = editableDoubleController(data.z)
    { _, new, _ -> data = data.copy(z = new) }

    val xScaleController = editableDoubleController(data.xScale)
    { _, new, _ -> data = data.copy(xScale = new) }

    val yScaleController = editableDoubleController(data.yScale)
    { _, new, _ -> data = data.copy(yScale = new) }

    val zScaleController = editableDoubleController(data.zScale)
    { _, new, _ -> data = data.copy(zScale = new) }

    val xRotationController = editableDoubleController(data.xRotation)
    { _, new, _ -> data = data.copy(xRotation = new) }

    val yRotationController = editableDoubleController(data.yRotation)
    { _, new, _ -> data = data.copy(yRotation = new) }

    val zRotationController = editableDoubleController(data.zRotation)
    { _, new, _ -> data = data.copy(zRotation = new) }

    val deleteCallbackProperty = DeleteCallbackProperty(onDelete, this) { old, new ->
        old?.let { header.right -= it }
        new?.let { header.right += it }
    }
    var onDelete by deleteCallbackProperty

    val header = Level(
        left = listOf(assetSelectController), mobile = true
    )

    override val container = Box(
            Columns(
                // first line
                Column(header, size = ColumnSize.Full),
                Column(opacityField, size = ColumnSize.Full),
                Column(Label("Position (x,y,z)"), size = ColumnSize.Full),
                Column(xController, size = ColumnSize.OneThird),
                Column(yController, size = ColumnSize.OneThird),
                Column(zController, size = ColumnSize.OneThird),
                Column(Label("Scale (x,y,z)"), size = ColumnSize.Full),
                Column(xScaleController, size = ColumnSize.OneThird),
                Column(yScaleController, size = ColumnSize.OneThird),
                Column(zScaleController, size = ColumnSize.OneThird),
                Column(Label("Rotation (x,y,z)"), size = ColumnSize.Full),
                Column(xRotationController, size = ColumnSize.OneThird),
                Column(yRotationController, size = ColumnSize.OneThird),
                Column(zRotationController, size = ColumnSize.OneThird),
                multiline = true
            )
        )

    override fun refresh() {
        urlController.refresh()
    }

}

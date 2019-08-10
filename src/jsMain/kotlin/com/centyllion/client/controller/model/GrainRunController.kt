package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Controller
import bulma.Label
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class GrainRunController(grain: Grain, model: GrainModel): Controller<Grain, GrainModel, Column> {

    val wrapped = GrainDisplayController(grain, model)

    override var data: Grain
        get() = wrapped.data
        set(value) { wrapped.data = value }

    override var context: GrainModel
        get() = wrapped.context
        set(value) { wrapped.context = value }

    override var readOnly: Boolean
        get() = wrapped.readOnly
        set(value) { wrapped.readOnly = value }

    val countLabel = Label().apply {
        wrapped.body.right += this
    }

    var count: Int by observable(-1) { _, _ , new ->
        countLabel.text = if (new < 0) "" else "$new"
    }

    override val container = Column(wrapped.container, size = ColumnSize.Full)

    override fun refresh() { wrapped.refresh() }
}

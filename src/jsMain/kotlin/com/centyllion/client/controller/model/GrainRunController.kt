package com.centyllion.client.controller.model

import bulma.Box
import bulma.Controller
import bulma.Label
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class GrainRunController(
    grain: Grain, model: GrainModel,
    wrapped: GrainDisplayController = GrainDisplayController(grain, model)
): Controller<Grain, GrainModel, Box> by wrapped {

    val countLabel = Label().apply {
        wrapped.body.right += this
    }

    var count: Int by observable(-1) { _, _ , new ->
        countLabel.text = if (new < 0) "" else "$new"
    }
}

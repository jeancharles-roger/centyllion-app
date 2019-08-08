package com.centyllion.client.controller.model

import bulma.Label
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class GrainRunController(grain: Grain, model: GrainModel): GrainDisplayController(grain, model) {

    val countLabel = Label()

    var count: Int by observable(-1) { _, _ , new ->
        countLabel.text = if (new < 0) "" else "$new"
    }

    init {
        body.right += countLabel
    }
}

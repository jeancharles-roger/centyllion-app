package com.centyllion.client.controller.model

import bulma.Box
import bulma.Controller
import bulma.Label
import com.centyllion.client.toFieldString
import com.centyllion.model.Field
import kotlin.properties.Delegates.observable


class FieldRunController(
    field: Field,
    wrapped: FieldDisplayController = FieldDisplayController(field)
): Controller<Field, Unit, Box> by wrapped {

    val floatLabel = Label().apply {
        wrapped.body.right += this
    }

    var amount: Float by observable(-1f) { _, _ , new ->
        floatLabel.text = new.toFieldString()
    }
}

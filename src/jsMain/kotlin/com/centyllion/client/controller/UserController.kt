package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.User
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class UserController(user: User?) : NoContextController<User?, BulmaElement>() {

    override var data: User? by observable(user) { _, _, _ ->
        newData = data
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            // TODO
        }
    }

    var newData: User? = data

    var onUpdate: ((old: User?, new: User?, UserController) -> Promise<Any>?) =
        { _, _, _ -> null }


    val nameValue = Value()
    val emailValue = Value()

    val saveResult = Help()
    val saveButton = textButton("Save Changes", ElementColor.Primary) {
        val result = onUpdate(data, newData, this@UserController)
        result?.then {
            data = newData
            saveResult.text = "Saved"
        }?.catch {
            saveResult.text = it.toString()
        }
    }

    override val container = div(
        Field(Label("Name"), nameValue, Help()),
        Field(Label("Email"), emailValue, Help()),
        Level(listOf(saveButton), listOf(saveResult))
    )


    init {
        refresh()
    }

    override fun refresh() {
        if (newData == null) {
            nameValue.text = ""
            emailValue.text = ""
            saveButton.disabled = true

        } else newData?.let {
            nameValue.text = it.name
            emailValue.text = it.email
            saveButton.disabled = data == newData
        }
    }
}

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
            descriptionController.readOnly = readOnly
        }
    }

    var newData: User? = data

    var onUpdate: ((old: User?, new: User?, UserController) -> Promise<Any>?) =
        { _, _, _ -> null }


    val nameValue = Value()
    val emailValue = Value()

    val descriptionController = multilineStringController("", "Description")

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

    override val container = Media(
        left = listOf(Image("https://bulma.io/images/placeholders/128x128.png", ImageSize.S128)),
        center = listOf(
            Columns(
                Column(
                    Field(Label("Name"), nameValue, Help()),
                    Field(Label("Email"), emailValue, Help()),
                    size = ColumnSize.S4
                ),
                Column(descriptionController, size = ColumnSize.S8),
                Column(Level(listOf(saveResult), listOf(saveButton)), size = ColumnSize.Full),
                multiline = true
            )
        )
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

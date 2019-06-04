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

    val nameController = EditableStringController(user?.name ?: "", readOnly = true)
    val emailController = EditableStringController(user?.email ?: "", readOnly = true)

    val subscription = Value("none")

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
                Column(nameController, emailController, subscription, size = ColumnSize.S4),
                Column(descriptionController, size = ColumnSize.S8),
                Column(Level(listOf(saveResult), listOf(saveButton)), size = ColumnSize.Full),
                multiline = true
            )
        )
    )

    override fun refresh() {
        if (newData == null) {
            nameController.text = ""
            emailController.text = ""
            saveButton.disabled = true

        } else newData?.let {
            nameController.text = it.name
            emailController.text = it.email
            saveButton.disabled = data == newData
        }
    }
}

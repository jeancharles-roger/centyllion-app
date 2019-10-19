package com.centyllion.client.controller.admin

import bulma.Box
import bulma.Image
import bulma.ImageSize
import bulma.Media
import bulma.NoContextController
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.User
import kotlin.properties.Delegates.observable

class UserAdministrationController(user: User, val page: BulmaPage) : NoContextController<User, Box>() {

    override var data: User by observable(user) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly: Boolean by observable(false) { _, _, _ -> }

    val nameController = EditableStringController(user.name, readOnly = true)
    val usernameController = EditableStringController(user.username, readOnly = true)
    val emailController = EditableStringController(user.details?.email ?: "", readOnly = true)

    override val container = Box(Media(
        left = listOf(Image("https://bulma.io/images/placeholders/128x128.png", ImageSize.S128)),
        center = listOf(nameController, usernameController, emailController)
    ))

    override fun refresh() {
        nameController.text = data.name
        usernameController.text = data.username
        emailController.text = data.details?.email ?: ""
    }
}

package com.centyllion.client.controller.navigation

import bulma.Box
import bulma.Image
import bulma.ImageSize
import bulma.Level
import bulma.Media
import bulma.NoContextController
import com.centyllion.client.AppContext
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.model.User
import com.centyllion.model.emptyUser
import kotlin.properties.Delegates.observable

class MeController(val appContext: AppContext) : NoContextController<User, Box>() {

    override var data: User by observable(appContext.me ?: emptyUser) { _, _, _ ->
        newData = data
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, _, _ -> }

    var newData: User = data

    val nameController = EditableStringController(data.name, readOnly = true)
    val usernameController = EditableStringController(data.username, readOnly = true)
    val emailController = EditableStringController(data.details?.email ?: "", readOnly = true)

    override val container = Box(Media(
        left = listOf(Image("https://bulma.io/images/placeholders/128x128.png", ImageSize.S128)),
        center = listOf(
            Level(left = listOf(usernameController)),
            nameController, emailController
        )
    ))

    override fun refresh() {
        nameController.text = newData.name
        usernameController.text = newData.username
        emailController.text = newData.details?.email ?: ""
    }
}

package com.centyllion.client.controller.navigation

import bulma.ElementColor
import bulma.Image
import bulma.ImageSize
import bulma.Media
import bulma.NoContextController
import bulma.Size
import bulma.Tag
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.common.SubscriptionType
import com.centyllion.model.User
import kotlin.properties.Delegates.observable

class UserController(user: User) : NoContextController<User, Media>() {

    override var data: User by observable(user) { _, _, _ ->
        newData = data
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, _, _ -> }

    var newData: User = data

    val nameController = EditableStringController(user.name, readOnly = true)
    val usernameController = EditableStringController(user.username, readOnly = true)
    val emailController = EditableStringController(user.details?.email ?: "", readOnly = true)

    val group = Tag(
        (newData.details?.subscription ?: SubscriptionType.Apprentice).name,
        color = ElementColor.Primary, rounded = true, size = Size.Medium
    )

    override val container = Media(
        left = listOf(Image("https://bulma.io/images/placeholders/128x128.png", ImageSize.S128)),
        center = listOf(nameController, usernameController, emailController, group)
    )

    override fun refresh() {
        nameController.text = newData.name
        usernameController.text = newData.username
        emailController.text = newData.details?.email ?: ""
        group.text = (newData.details?.subscription ?: SubscriptionType.Apprentice).name
    }
}

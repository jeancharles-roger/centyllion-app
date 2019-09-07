package com.centyllion.client.controller.navigation

import bulma.Box
import bulma.Control
import bulma.ElementColor
import bulma.Field
import bulma.Icon
import bulma.Image
import bulma.ImageSize
import bulma.Level
import bulma.Media
import bulma.NoContextController
import bulma.Size
import bulma.Tag
import bulma.TextColor
import bulma.iconButton
import com.centyllion.client.AppContext
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.common.SubscriptionType
import com.centyllion.common.roleColors
import com.centyllion.common.roleIcons
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

    val group = Tag(
        (newData.details?.subscription ?: SubscriptionType.Apprentice).name,
        color = ElementColor.Primary, rounded = true, size = Size.Medium
    )

    fun roleColor(role: String) = (roleColors[role] ?: "is-primary").let { c ->
        ElementColor.values().find { c == it.className }
    } ?: ElementColor.Primary

    fun roleButtons() = roleIcons.map {
        appContext.hasRole(it.key).let { hasRole ->
            Control(
                iconButton(
                    Icon(it.value, color = if (hasRole) TextColor.White else TextColor.GreyLighter),
                    if (hasRole) roleColor(it.key) else ElementColor.White,
                    rounded = true, size = Size.None
                )
            )
        }
    }

    val roles = Field(grouped = true).apply { body = roleButtons() }

    override val container = Box(Media(
        left = listOf(Image("https://bulma.io/images/placeholders/128x128.png", ImageSize.S128)),
        center = listOf(
            Level(left = listOf(group, usernameController), right = listOf(roles)),
            nameController, emailController
        )
    ))

    override fun refresh() {
        nameController.text = newData.name
        usernameController.text = newData.username
        emailController.text = newData.details?.email ?: ""
        group.text = (newData.details?.subscription ?: SubscriptionType.Apprentice).name
        roles.body = roleButtons()
    }
}

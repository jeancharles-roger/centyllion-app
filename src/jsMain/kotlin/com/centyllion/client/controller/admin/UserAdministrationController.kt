package com.centyllion.client.controller.admin

import bulma.*
import com.centyllion.client.Api
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.common.allRoles
import com.centyllion.model.User
import kotlin.properties.Delegates.observable

class UserAdministrationController(user: User, api: Api) : NoContextController<User, BulmaElement>() {

    override var data: User by observable(user) { _, _, _ ->
        newData = data
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, _, _ -> }

    var newData: User = data

    val nameController = EditableStringController(user.name, readOnly = true)
    val usernameController = EditableStringController(user.username, readOnly = true)
    val emailController = EditableStringController(user.details?.email ?: "", readOnly = true)

    val subscription = Tags(roles())

    val button = textButton("+") { button ->
        api.createSubscriptionForUser(user.id).then {
            api.fetchSubscriptionsForUser(user.id).then { button.text = "${it.count()}" }
        }
    }.apply {
        api.fetchSubscriptionsForUser(user.id).then { this.text = "${it.count()}" }
    }

    override val container = Media(
        left = listOf(Image("https://bulma.io/images/placeholders/128x128.png", ImageSize.S128)),
        center = listOf(nameController, usernameController, emailController, subscription),
        right = listOf(button)
    )

    fun roles() = newData.details?.roles
        ?.mapNotNull { allRoles[it] }
        ?.map { Tag(it, color = ElementColor.Primary, rounded = true, size = Size.Medium) }
        ?: emptyList()

    override fun refresh() {
        nameController.text = newData.name
        usernameController.text = newData.username
        emailController.text = newData.details?.email ?: ""
        subscription.tags = roles()
    }
}

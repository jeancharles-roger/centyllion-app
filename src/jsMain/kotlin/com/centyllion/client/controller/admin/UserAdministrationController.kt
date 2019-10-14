package com.centyllion.client.controller.admin

import bulma.Box
import bulma.Button
import bulma.Checkbox
import bulma.ElementColor
import bulma.Help
import bulma.Icon
import bulma.Image
import bulma.ImageSize
import bulma.Level
import bulma.Media
import bulma.NoContextController
import bulma.Option
import bulma.Select
import bulma.Size
import bulma.Tag
import bulma.extension.Slider
import bulma.textButton
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.page.BulmaPage
import com.centyllion.common.SubscriptionType
import com.centyllion.model.SubscriptionParameters
import com.centyllion.model.SubscriptionState
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

    val group = Tag(
        (data.details?.subscription ?: SubscriptionType.Apprentice).name,
        color = ElementColor.Primary, rounded = true, size = Size.Medium
    )

    val createSubscription = Button(page.i18n("Subscription"), Icon("plus"), ElementColor.Info, true) { _ ->

        val autoRenew = Checkbox(page.i18n("auto renew"))
        val options = SubscriptionType.values().filter { it != SubscriptionType.Apprentice }.map { Option(it.name) }
        val subscription = Select(options, rounded = true)
        val durationLabel = Help(page.i18n("%0 days", 31))
        val duration = Slider("31", "0", "365", "1", ElementColor.Link) { _, value ->
            durationLabel.text = if (value == "0") page.i18n("no bound") else page.i18n("%0 days", value)
        }

        val createButton = textButton(page.i18n("Create"), ElementColor.Success) { _ ->
            val type = SubscriptionType.valueOf(subscription.selectedOption.text)
            val durationMillis = duration.value.toLong() * (24 * 60 * 60 * 1_000)
            val parameters = SubscriptionParameters(autoRenew.checked, type, durationMillis, 0.0,"manual")
            page.appContext.api.createSubscriptionForUser(data.id, parameters).then {
                group.text = it.subscription.name
                group.color = if (it.state == SubscriptionState.Waiting) ElementColor.Warning else ElementColor.Success
                page.message("Subscription %0 created.", parameters.subscription)
            }.catch { page.error(it) }
        }
        val cancelButton = textButton(page.i18n("Cancel"))

        page.modalDialog(
            page.i18n("Create subscription"),
            listOf(Level(center = listOf(autoRenew, subscription, duration, durationLabel))),
            createButton, cancelButton
        )
    }

    override val container = Box(Media(
        left = listOf(Image("https://bulma.io/images/placeholders/128x128.png", ImageSize.S128)),
        center = listOf(nameController, usernameController, emailController, group),
        right = listOf(createSubscription)
    ))

    override fun refresh() {
        nameController.text = data.name
        usernameController.text = data.username
        emailController.text = data.details?.email ?: ""
        group.text = (data.details?.subscription ?: SubscriptionType.Apprentice).name
    }
}

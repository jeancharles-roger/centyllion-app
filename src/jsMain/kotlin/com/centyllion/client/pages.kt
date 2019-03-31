package com.centyllion.client

import KeycloakInstance
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import com.centyllion.client.controller.ModelPage
import com.centyllion.client.controller.UserController
import com.centyllion.common.adminRole
import com.centyllion.common.modelRole
import com.centyllion.model.Action
import kotlinx.html.article
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.p
import org.w3c.dom.HTMLElement
import kotlin.browser.document

data class Page(
    val title: String,
    val id: String,
    val role: String,
    val callback: (root: HTMLElement, instance: KeycloakInstance) -> Unit
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = if (role != "") keycloak.hasRealmRole(role) else true
}

const val contentSelector = "section.cent-main"

val pages = listOf(
    Page("Model", "model", modelRole, ::model),
    Page("Profile", "profile", "", ::profile),
    Page("Administration", "administration", adminRole, ::administration)
)

fun profile(root: HTMLElement, instance: KeycloakInstance) {
    val userController = UserController()
    val columns = Columns(Column(userController.container, size = ColumnSize.TwoThirds))
    root.appendChild(columns.root)

    // initialize controller
    fetchUser(instance).then { userController.data = it }

    // sets callbacks for update
    userController.onUpdate = { _, new, _ ->
        if (new != null) saveUser(new, instance) else null
    }
}

fun model(root: HTMLElement, instance: KeycloakInstance) {
    root.appendChild(ModelPage(instance).root)
}

fun administration(root: HTMLElement, instance: KeycloakInstance) {
    fetchEvents(instance).then { events ->
        events.forEach {
            val color = when (it.action) {
                Action.Create -> "is-primary"
                Action.Save -> "is-info"
                Action.Delete -> "is-warning"
            }
            root.appendChild(document.create.article("message $color") {
                div("message-header level") {
                    div("level-left") {
                        div("level-item") { +"${it.action} on ${it.collection}" }
                    }
                    div("level-right") {
                        div("level-item") { +it.date }
                    }
                }
                div("message-body") {
                    it.arguments.forEach {
                        p { +it }
                    }
                }
            })
        }
    }
}


package com.centyllion.client.controller

import com.centyllion.model.User
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class UserController : Controller<User?> {

    override var data: User? by observable<User?>(null) { _, _, _ ->
        newData = data
        refresh()
    }

    var newData: User? = data

    var onUpdate: ((old: User?, new: User?, UserController) -> Promise<Any>?) =
        { _, _, _ -> null }

    override val container: HTMLDivElement = document.create.div {
        div("field cent-user-name") {
            label("label") { +"Name" }
            span("value") {}
            p("help") {}
        }
        div("field cent-user-email") {
            label("label") { +"Email" }
            span("value") {}
            p("help") {}
        }

        div("level cent-save") {
            a(classes = "button is-primary") {
                +"Save changes"
                onClickFunction = {
                    val result = onUpdate(data, newData, this@UserController)
                    result?.then {
                        data = newData
                        saveResult.innerText = "Saved"
                    }?.catch {
                        saveResult.innerText = it.toString()
                    }
                }
            }
            p("help")
        }
    }

    val name = container.querySelector("div.cent-user-name > .value") as HTMLSpanElement
    val email = container.querySelector("div.cent-user-email > .value") as HTMLSpanElement

    val save = container.querySelector("div.cent-save > a") as HTMLAnchorElement
    val saveResult = container.querySelector("div.cent-save > .help") as HTMLParagraphElement

    init {
        refresh()
    }

    override fun refresh() {
        if (newData == null) {
            name.innerText = ""
            email.innerText = ""
            save.setAttribute("disabled", "")

        } else newData?.let {
            name.innerText = it.name
            email.innerText = it.email
            if (data == newData) {
                save.setAttribute("disabled", "")
            } else {
                save.removeAttribute("disabled")
            }
        }
    }
}

package com.centyllion.client

import Keycloak
import KeycloakInitOptions
import KeycloakInstance
import com.centyllion.common.betaRole
import com.centyllion.common.centyllionHost
import kotlinx.html.*
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise

fun activateNavBar() {
    val all = document.querySelectorAll(".navbar-burger")
    for (i in 0 until all.length) {
        val burger = all[i] as HTMLElement
        burger.addEventListener("click", { _ ->
            // Get the target from the "data-target" attribute
            val target = burger.dataset["target"]
            if (target != null) {
                val targetElement = document.getElementById(target) as HTMLElement
                burger.classList.toggle("is-active")
                targetElement.classList.toggle("is-active")
            }
        })
    }
}

fun authenticate(required: Boolean): Promise<KeycloakInstance?> {
    val keycloak = Keycloak()
    val options = KeycloakInitOptions(checkLoginIframe = false, promiseType = "native")
    options.onLoad = if (required) "login-required" else "check-sso"
    val promise = keycloak.init(options)
    return promise.then(onFulfilled = { keycloak }, onRejected = { null })
}

fun initialize(vararg roles: String): Promise<KeycloakInstance?> {
    activateNavBar()
    showVersion()

    val isCentyllionHost = window.location.host == centyllionHost
    val requiredRoles = if (!isCentyllionHost) roles + betaRole else roles

    return authenticate(requiredRoles.isNotEmpty()).then { keycloak ->
        if (keycloak != null) {
            if (keycloak.tokenParsed != null) {
                val userName = document.querySelector("a.cent-user") as HTMLAnchorElement?
                userName?.innerText = keycloak.tokenParsed.asDynamic().name as String
                userName?.href = keycloak.createAccountUrl()
            }
            val granted = keycloak.authenticated &&
                    requiredRoles.fold(true) { a, r -> a && keycloak.hasRealmRole(r) }

            if (granted) keycloak else null
        } else {
            null
        }
    }
}

data class ColumnSize(
    val desktop: Int, val tablet: Int = desktop, val mobile: Int? = null, val centered: Boolean = false
) {
    val classes = "column is-$desktop-desktop is-$tablet-tablet " +
            if (mobile != null) "is-$mobile-mobile" else "" +
                    if (centered) "is-centered" else ""
}

fun size(desktop: Int, tablet: Int = desktop, mobile: Int? = null, centered: Boolean = false) =
    ColumnSize(desktop, tablet, mobile, centered)

fun column(content: HTMLElement, size: ColumnSize = size(4)): HTMLDivElement {
    val result = document.createElement("div") as HTMLDivElement
    result.className = "column ${size.classes}"
    result.appendChild(content)
    return result
}

@HtmlTagMarker
fun <T, C : TagConsumer<T>> C.columns(classes: String = "", block: DIV.() -> Unit = {}) =
    DIV(attributesMapOf("class", "columns $classes"), this).visitAndFinalize(this, block)

@HtmlTagMarker
fun FlowContent.columns(classes: String = "", block: DIV.() -> Unit = {}) =
    DIV(attributesMapOf("class", "columns $classes"), consumer).visit(block)

@HtmlTagMarker
fun FlowContent.column(size: ColumnSize = ColumnSize(4), classes: String = "", block: DIV.() -> Unit = {}) =
    DIV(attributesMapOf("class", "${size.classes} $classes"), consumer).visit(block)

@HtmlTagMarker
fun <T, C : TagConsumer<T>> C.column(size: ColumnSize = ColumnSize(4), classes: String = "", block: DIV.() -> Unit = {}) =
    DIV(attributesMapOf("class", "columns ${size.classes} $classes"), this).visitAndFinalize(this, block)


fun <T> Array<T>.push(e: T): Int = asDynamic().push(e) as Int
fun <T> Array<T>.pop(): T = asDynamic().pop() as T

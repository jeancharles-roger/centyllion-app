package com.centyllion.client

import Keycloak
import KeycloakInitOptions
import KeycloakInstance
import bulma.Title
import com.centyllion.common.betaRole
import com.centyllion.common.centyllionHost
import kotlinx.html.a
import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.js.onClickFunction
import org.w3c.dom.*
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise

@JsName("index")
fun index() {
    initialize().then { (instance, page) ->
        console.log("Starting function")
        if (page == null) activatePage(mainPage, instance)
    }.catch {
        val root = document.querySelector(contentSelector) as HTMLElement
        root.appendChild(Title("Problem during connection ${it.message}").root)

        console.error("Error on initialize")
        console.error(it)
    }
}

fun authenticate(required: Boolean): Promise<KeycloakInstance?> {
    val keycloak = Keycloak()
    val options = KeycloakInitOptions(checkLoginIframe = false, promiseType = "native")
    options.onLoad = if (required) "login-required" else "check-sso"
    val promise = keycloak.init(options)
    return promise.then(onFulfilled = { keycloak }, onRejected = { null })
}

fun initialize(vararg roles: String): Promise<Pair<KeycloakInstance?, Page?>> {
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

            // gets params active page if any to activate it is allowed
            val params = URLSearchParams(window.location.search)
            val page = params.get("page")?.let { id -> pages.find { it.id == id } }
            if (page != null) activatePage(page, keycloak)

            addMenu(isCentyllionHost, keycloak)
            (if (granted) keycloak else null) to page
        } else {
            null to null
        }
    }
}

fun updateActivePage(page: Page) {
    // clear active status
    val menu = document.querySelector(".navbar-menu > .navbar-start") as HTMLDivElement
    for (item in menu.querySelectorAll(".navbar-item").asList()) {
        if (item is HTMLElement) {
            item.classList.remove("has-text-weight-bold")
        }
    }
    // adds active status to current menu
    val item = menu.querySelector("a.cent-${page.id}")
    if (item is HTMLElement) {
        item.classList.add("has-text-weight-bold")
    }

    // update page parameter in URL
    window.location.let {
        val params = URLSearchParams(it.search)
        params.set("page", page.id)
        val newUrl = "${it.protocol}//${it.host}${it.pathname}?$params"
        window.history.pushState(null, "Centyllion ${page.title}", newUrl)
    }
}

fun activatePage(page: Page, instance: KeycloakInstance?) {
    updateActivePage(page)

    val root = document.querySelector(contentSelector) as HTMLElement
    root.innerHTML = ""
    if (instance != null && page.authorized(instance)) {
        page.callback(root, instance)
    } else {
        root.appendChild(document.create.h1("title") {
            +"Not authorized"
        })
    }
}

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

fun addMenu(isCentyllionHost: Boolean, keycloak: KeycloakInstance) {
    if (isCentyllionHost || keycloak.hasRealmRole(betaRole)) {
        val menu = document.querySelector(".navbar-menu > .navbar-start") as HTMLDivElement
        pages.filter { page -> page.authorized(keycloak) }
            .forEach { page ->
                menu.appendChild(document.create.a(classes = "navbar-item cent-${page.id}") {
                    +page.title
                    onClickFunction = {
                        activatePage(page, keycloak)
                    }
                })
            }
    }
}

package com.centyllion.client

import Keycloak
import KeycloakInitOptions
import KeycloakInstance
import bulma.ElementColor
import bulma.Message
import bulma.Title
import bulma.span
import kotlinx.html.a
import kotlinx.html.dom.create
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
        if (page == null) openPage(mainPage, instance)
    }.catch {
        val root = document.querySelector(contentSelector) as HTMLElement
        error(root, it)
        console.error("Error on initialize")
        console.error(it)
    }
}

/** Open the given [page] */
fun openPage(
    page: Page, instance: KeycloakInstance?,
    parameters: Map<String, String> = emptyMap(), register: Boolean = true
) {
    // highlight active page
    updateActivePage(page)

    // updates locations and register to history
    updateLocation(page, parameters, register)

    // gets root element
    val root = document.querySelector(contentSelector) as HTMLElement
    // clears element
    while (root.hasChildNodes()) {
        root.removeChild(root.childNodes[0]!!)
    }

    // tries to load page if authorized
    if (page.authorized(instance)) {
        page.callback(root, instance)
    } else {
        error(root, "Unauthorized", "You are not authorized to access this page")
    }
}

/** Updates location with given [page] and [parameters]. It can also [register] the location to the history. */
fun updateLocation(page: Page?, parameters: Map<String, String>, register: Boolean) {
    window.location.let {
        // sets parameters
        val params = URLSearchParams(it.search)
        parameters.forEach { params.set(it.key, it.value) }
        val currentPage = if (page != null) {
            params.set("page", page.id)
            page
        } else {
            pages.find { it.id == params.get("page") }
        }

        // registers page to history if needed
        if (register) {
            val newUrl = "${it.protocol}//${it.host}${it.pathname}?$params"
            window.history.pushState(null, "Centyllion ${currentPage?.title}", newUrl)
        }
    }
}

fun getLocationParams(name: String) = URLSearchParams(window.location.search).get(name)

fun authenticate(required: Boolean): Promise<KeycloakInstance?> {
    val keycloak = Keycloak()
    val options = KeycloakInitOptions(checkLoginIframe = false, promiseType = "native", timeSkew = 60)
    options.onLoad = if (required) "login-required" else "check-sso"
    val promise = keycloak.init(options)
    return promise.then(onFulfilled = { keycloak }, onRejected = { null })
}

fun findPageInUrl(): Page? {
    // gets params active page if any to activate it is allowed
    val params = URLSearchParams(window.location.search)
    return params.get("page")?.let { id -> pages.find { it.id == id } }
}

fun initialize(vararg roles: String): Promise<Pair<KeycloakInstance?, Page?>> {
    activateNavBar()
    showVersion()

    val requiredRoles = roles

    return authenticate(requiredRoles.isNotEmpty()).then { keycloak ->
        if (keycloak != null) {
            val userLink = document.querySelector("a.cent-user") as HTMLAnchorElement?
            if (keycloak.tokenParsed != null) {
                userLink?.let {
                    it.innerText = keycloak.tokenParsed.asDynamic().name as String
                    it.href = keycloak.createAccountUrl()
                }
            } else {
                userLink?.let {
                    it.innerText = "Log in"
                    it.href = keycloak.createLoginUrl(null)
                }
            }

            val granted = keycloak.authenticated &&
                    requiredRoles.fold(true) { a, r -> a && keycloak.hasRealmRole(r) }

            listenToPopstate(keycloak)
            addMenu(keycloak)

            val page = findPageInUrl()
            if (page != null) openPage(page, keycloak, register = false)

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

fun addMenu(keycloak: KeycloakInstance) {
    val menu = document.querySelector(".navbar-menu > .navbar-start") as HTMLDivElement
    pages.filter { page -> page.header && page.authorized(keycloak) }
        .forEach { page ->
            menu.appendChild(document.create.a(classes = "navbar-item cent-${page.id}") {
                +page.title
                onClickFunction = {
                    openPage(page, keycloak)
                }
            })
        }
}

fun listenToPopstate(instance: KeycloakInstance) {
    window.addEventListener("popstate", {
        findPageInUrl()?.let { openPage(it, instance, register = false) }
    })
}

fun error(root: HTMLElement, throwable: Throwable) =
    error(root, "Error: ${throwable::class.simpleName}", throwable.message.toString())

fun error(root: HTMLElement, title: String, content: String) {
    val message = Message(color = ElementColor.Danger, header = listOf(Title(title)), body = listOf(span(content)))
    root.appendChild(message.root)
}

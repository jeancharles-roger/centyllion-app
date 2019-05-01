package com.centyllion.client

import Keycloak
import KeycloakInitOptions
import bulma.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.document
import kotlin.browser.window

@JsName("index")
fun index() {
    // creates nav bar and adds it to body
    val navBar = NavBar(
        brand = listOf(NavBarImageItem("images/logo-2by1.png", "/")),
        end = listOf(NavBarLinkItem("Not connected"))
    )
    document.body?.insertAdjacentElement(Position.AfterBegin.toString(), Section(Container(navBar)).root)

    val root = document.querySelector(contentSelector) as HTMLElement

    // creates keycloak instance
    val keycloak = Keycloak()

    // creates context
    val context = object : AppContext {
        override val navBar = navBar
        override val root = root
        override val keycloak = keycloak
        override val api = Api(keycloak)

        override fun error(throwable: Throwable) {
            fun findCause(throwable: Throwable): Throwable = throwable.cause?.let { findCause(it) } ?: throwable
            error(findCause(throwable).message.toString())
        }

        override fun error(content: String) = notification(root, content, ElementColor.Danger)

        override fun warning(content: String) = notification(root, content, ElementColor.Warning)

        override fun message(content: String) = notification(root, content, ElementColor.None, true)
    }

    val options = KeycloakInitOptions(checkLoginIframe = false, promiseType = "native", onLoad = "check-sso")
    keycloak.init(options).then { success ->

        // updates login link
        val userItem = navBar.end.first() as NavBarLinkItem
        if (keycloak.tokenParsed != null) {
            userItem.text = keycloak.tokenParsed.asDynamic().name as String
            userItem.href = keycloak.createAccountUrl()
        } else {
            userItem.text = "Log In"
            userItem.href = keycloak.createLoginUrl(null)
        }

        // listens to pop state
        window.addEventListener("popstate", {
            findPageInUrl()?.let { openPage(it, context, register = false) }
        })

        // adds menu
        context.navBar.start = pages.filter { page -> page.header && page.authorized(context.keycloak) }
            .map { page -> NavBarLinkItem(page.title, id = page.id) { openPage(page, context) } }

        // shows version
        showVersion(context.api)

        console.log("Starting function")

        openPage(findPageInUrl() ?: mainPage, context, register = false)

    }.catch {
        context.error(it)
        console.error("Error on initialize")
        console.error(it.asDynamic().stack)
    }
}

/** Open the given [page] */
fun openPage(
    page: Page, appContext: AppContext,
    parameters: Map<String, String> = emptyMap(), register: Boolean = true
) {
    // highlight active page
    appContext.navBar.start.forEach {
        if (it.root.id == page.id) {
            it.root.classList.add("has-text-weight-bold")
            it.root.classList.add("is-active")

        } else {
            it.root.classList.remove("has-text-weight-bold")
            it.root.classList.remove("is-active")
        }
    }

    // updates locations and register to history
    updateLocation(page, parameters, register)

    // gets root element
    val root = document.querySelector(contentSelector) as HTMLElement
    // clears element
    while (root.hasChildNodes()) {
        root.removeChild(root.childNodes[0]!!)
    }

    // tries to load page if authorized
    if (page.authorized(appContext.keycloak)) {
        page.callback(appContext)
    } else {
        appContext.error("You are not authorized to access this page")
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

fun findPageInUrl(): Page? {
    // gets params active page if any to activate it is allowed
    val params = URLSearchParams(window.location.search)
    return params.get("page")?.let { id -> pages.find { it.id == id } }
}

private fun notification(
    root: HTMLElement, content: String, color: ElementColor = ElementColor.None, autoDelete: Boolean = false
) {
    fun deleteNotification(it: Notification) { it.root.parentElement?.removeChild(it.root) }
    val notification = Notification(span(content, "is-size-6"), color = color, onDelete = ::deleteNotification)
    if (autoDelete) window.setTimeout( {deleteNotification(notification) }, 1000)
    root.insertAdjacentElement(Position.AfterBegin.toString(), notification.root)
}

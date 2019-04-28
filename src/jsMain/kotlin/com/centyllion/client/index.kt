package com.centyllion.client

import Keycloak
import KeycloakInitOptions
import KeycloakInstance
import bulma.*
import kotlinx.io.IOException
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise

@JsName("index")
fun index() {
    // creates nav bar and adds it to body
    val navBar = navBar()
    document.body?.insertAdjacentElement(Position.AfterBegin.toString(), Section(Container(navBar)).root)

    val root = document.querySelector(contentSelector) as HTMLElement
    authenticate(false).then { keycloak ->

        // creates context
        val context = object : AppContext {
            override val navBar = navBar
            override val root = root
            override val keycloak = keycloak
            override val api = Api(keycloak)
        }

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
        error(root, it)
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

fun authenticate(required: Boolean): Promise<KeycloakInstance> {
    val keycloak = Keycloak()
    val options = KeycloakInitOptions(checkLoginIframe = false, promiseType = "native", timeSkew = 60)
    options.onLoad = if (required) "login-required" else "check-sso"
    val promise = keycloak.init(options)
    return promise.then(onFulfilled = { keycloak }, onRejected = { throw IOException("Can't connect to Keycloak") })
}

fun findPageInUrl(): Page? {
    // gets params active page if any to activate it is allowed
    val params = URLSearchParams(window.location.search)
    return params.get("page")?.let { id -> pages.find { it.id == id } }
}

fun navBar(): NavBar {
    val navBar = NavBar(
        end = listOf(
            NavBarLinkItem("Not connected")
        )
    )
    navBar.brand = listOf(
        NavBarImageItem("images/logo-2by1.png"),
        NavBarBurger() {
            it.active = !it.active
            navBar.menuNode.classList.toggle("is-active")
        }
    )
    return navBar
}

fun error(root: HTMLElement, throwable: Throwable) =
    error(root, "Error: ${throwable::class.simpleName}", throwable.message.toString())

fun error(root: HTMLElement, title: String, content: String) {
    val message = Message(color = ElementColor.Danger, header = listOf(Title(title)), body = listOf(span(content)))
    root.appendChild(message.root)
}

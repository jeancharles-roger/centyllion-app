package com.centyllion.client

import bulma.*
import bulmatoast.ToastAnimation
import bulmatoast.ToastOptions
import bulmatoast.bulmaToast
import keycloak.Keycloak
import keycloak.KeycloakInitOptions
import keycloak.KeycloakInstance
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date

@JsName("index")
fun index() {
    // creates nav bar and adds it to body
    val navBar = NavBar(
        brand = listOf(NavBarImageItem("https://www.centyllion.com/assets/images/logo-2by1.png", "/")),
        end = listOf(NavBarLinkItem("Not connected")), transparent = true
    )
    document.body?.insertAdjacentElement(Position.AfterBegin.value,navBar.root)

    val root = document.querySelector(contentSelector) as HTMLElement

    // creates keycloak instance
    val keycloak = Keycloak()

    // creates context
    val context = BrowserContext(navBar, root, keycloak)

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
    page: Page, appContext: AppContext, parameters: Map<String, String> = emptyMap(),
    clearParameters: Boolean = true, register: Boolean = true
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
    updateLocation(page, parameters, clearParameters, register)

    // gets root element
    val root = document.querySelector(contentSelector) as HTMLElement
    // clears element
    while (root.hasChildNodes()) {
        root.removeChild(root.childNodes[0]!!)
    }

    // tries to load page if authorized
    if (page.authorized(appContext.keycloak)) {
        appContext.root.appendChild(page.callback(appContext).root)
    } else {
        appContext.error("You are not authorized to access this page")
    }
}

/** Updates location with given [page] and [parameters]. It can also [register] the location to the history. */
fun updateLocation(page: Page?, parameters: Map<String, String>, clearParameters: Boolean, register: Boolean) {
    window.location.let {
        // sets parameters
        val params = URLSearchParams(if (clearParameters) "" else it.search)
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


class BrowserContext(
    override val navBar: NavBar,
    override val root: HTMLElement,
    override val keycloak: KeycloakInstance
) : AppContext {

    private val storedEvents = mutableListOf<ClientEvent>()

    override val api = Api(keycloak)

    override val events: List<ClientEvent> get() = storedEvents

    override fun error(throwable: Throwable) {
        fun findCause(throwable: Throwable): Throwable = throwable.cause?.let { findCause(it) } ?: throwable
        error(findCause(throwable).message.toString())
    }

    override fun error(content: String) = event(content, ElementColor.Danger)

    override fun warning(content: String) = event(content, ElementColor.Warning)

    override fun message(content: String) = event(content, ElementColor.None)

    private fun event(content: String, color: ElementColor) {
        val date = Date().toISOString()
        val event = ClientEvent(date, content, color)
        storedEvents.add(event)
        notification(event.context, event.color)
    }
}

private fun notification(content: String, color: ElementColor = ElementColor.None) {
    when (color) {
        ElementColor.Danger -> console.error(content)
        ElementColor.Warning -> console.warn(content)
        else -> console.log(content)
    }

    val animation = ToastAnimation("fadeIn", "fadeOut")
    val options = ToastOptions(
        content, color.className, 2000, "bottom-center",
        false, true, true, 0.8, animation
    )
    bulmaToast.toast(options)
}

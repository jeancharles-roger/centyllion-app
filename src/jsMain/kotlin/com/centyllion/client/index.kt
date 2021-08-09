package com.centyllion.client

import bulma.*
import com.centyllion.client.page.BulmaPage
import com.centyllion.i18n.Locale
import com.centyllion.model.User
import keycloak.BasicKeycloakInitOptions
import keycloak.Keycloak
import keycloak.KeycloakInstance
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.url.URLSearchParams
import kotlin.js.Promise

@Serializable
data class CssFile(
    val files: List<String>
)

fun createNavBar(locale: Locale) = NavBar(
    brand = listOf(
        NavBarImageItem(
            "https://www.centyllion.com/assets/images/logo-white-2by1.png",
            "https://centyllion.com"
        ).apply { imgNode.style.maxHeight = "2rem" }
    ),
    end = listOf(
        NavBarIconItem(Icon("book"), "https://centyllion.com/fr/documentation.html").apply {
            root.asDynamic().target = "_blank"
            setTooltip(locale.i18n("Documentation"))
        },
        NavBarIconItem(Icon("envelope"), "mailto:bug@centyllion.com").apply {
            setTooltip(locale.i18n("Contact Us"))
        }
    ),
    transparent = true, color = ElementColor.Primary
)

fun startApp(page: Page?, context: AppContext) {
    val keycloak = context.keycloak
    val navBar = context.navBar

    // updates login link
    if (keycloak.tokenParsed != null) {
        val token = keycloak.tokenParsed.asDynamic()
        navBar.end += NavBarLinkItem(
            token.name as String? ?: token.preferred_username as String? ?: context.i18n("Anonymous"),
            keycloak.createAccountUrl()
        )
        navBar.end += NavBarLinkItem(context.i18n("Logout"), keycloak.createLogoutUrl())
    } else {
        navBar.end += NavBarLinkItem(context.i18n("Register"), keycloak.createRegisterUrl())
        navBar.end += NavBarLinkItem(context.i18n("Log In"), keycloak.createLoginUrl())
    }

    // listens to pop state
    window.addEventListener("popstate", {
        findPageInUrl()?.let { context.openPage(it, register = false) }
    })

    // adds menu
    context.navBar.start += pages
        .filter { it.header && it.authorized(context) }
        .map { NavBarLinkItem(context.i18n(it.titleKey), id = it.id) { _ -> context.openPage(it) } }

    showInfo(context)

    // shows version
    showVersion(context.api)

    console.log("Starting function")

    val selectedPage = page ?: if (context.me != null) homePage else explorePage
    context.openPage(selectedPage, register = false)
}

fun appendErrorMessage(root: HTMLElement, throwable: Throwable?) {
    fun findCause(throwable: Throwable?): Throwable? = throwable?.cause?.let { findCause(it) } ?: throwable
    findCause(throwable).let {
        appendErrorMessage(root, it?.message ?: "")
        console.error(it?.asDynamic()?.stack)
    }
}

fun appendErrorMessage(root: HTMLElement, message: String) {
    root.appendChild(
        Message(
            listOf(span("Error")),
            listOf(span(message)),
            color = ElementColor.Danger
        ).root
    )
    console.error("Error: $message")
}


fun main() = index()

//@JsName("index")
fun index() {
    console.log("Starting app")
    val root = document.querySelector(contentSelector) as HTMLElement

    // creates keycloak instance
    val keycloak = Keycloak("/keycloak.json")

    val api = Api(keycloak)
    api.addCss()

    api.fetchLocales().then { locales ->
        val localeName = locales.resolve(window.navigator.language)
        console.log("Loading locale $localeName for ${window.navigator.language}")
        api.fetchLocale(localeName).then { locale ->

            val navBar = createNavBar(locale)
            document.body?.insertAdjacentElement(Position.AfterBegin.value, navBar.root)

            val page = findPageInUrl()
            val options = BasicKeycloakInitOptions(
                promiseType = "native",
                onLoad = if (page?.needUser == true) "login-required" else "check-sso",
                //checkLoginIframe = false,
                timeSkew = 10
            )
            val t = keycloak.init(options)
            console.log(t)
            t.then { api.fetchMe() }
                .then { user ->
                    // creates context
                    val context = BrowserContext(locale, navBar, keycloak, user, api)

                    // adds add model button in navbar
                    navBar.start += NavBarContentItem(
                        Button(locale.i18n("Model"), Icon("plus"), ElementColor.Link, size = Size.Small) {
                            context.openPage(showPage)
                        }
                    )

                    navBar.end = listOf(NavBarIconItem(Icon("question-circle")) {
                        context.openPage(showPage, mapOf("tutorial" to ""))
                    }.apply {
                        setTooltip(locale.i18n("Start tutorial"))
                    }) + navBar.end

                    startApp(page, context)

                }.catch { appendErrorMessage(root, it) }

        }.catch {
            console.error(it)
            appendErrorMessage(root, "Locale file for $localeName couldn't be loaded")
        }

    }.catch {appendErrorMessage(root, "Locales file couldn't be loaded") }
}

fun findPageInUrl(): Page? = pages.find { it.id == window.location.pathname }

class BrowserContext(
    override val locale: Locale,
    override val navBar: NavBar,
    override val keycloak: KeycloakInstance,
    override val me: User?,
    override val api: Api = Api(keycloak)
) : AppContext {

    private var addedNavbarEndItems: List<BulmaElement> = emptyList()
    private var content: BulmaPage? = null
    private var currentPage: Page? = null

    private val storedEvents = mutableListOf<ClientEvent>()

    override val events: List<ClientEvent> get() = storedEvents

    override fun notify(event: ClientEvent) {
        storedEvents.add(event)
    }

    /** Open the given [page] */
    override fun openPage(page: Page, parameters: Map<String, String>, clearParameters: Boolean, register: Boolean) {
        val open = content?.onExit() ?: Promise.resolve(true)
        open.then { proceed ->
            if (proceed) {
                currentPage = page

                // highlight active page
                navBar.start.forEach {
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

                // resets navbar end items
                navBar.end = navBar.end.filter { !addedNavbarEndItems.contains(it) }
                addedNavbarEndItems = emptyList()

                // tries to load page if authorized
                if (page.authorized(this)) {
                    content = page.callback(this).also {
                        root.appendChild(it.root)
                        addedNavbarEndItems = it.navBarItem()
                        navBar.end = addedNavbarEndItems + navBar.end
                    }
                } else {
                    root.appendChild(
                        Message(
                            listOf(span("Error")),
                            listOf(span("You are not authorized to access this page")),
                            color = ElementColor.Danger
                        ).root
                    )
                }
            }
        }
    }

    /** Updates location with given [page] and [parameters]. It can also [register] the location to the history. */
    private fun updateLocation(page: Page?, parameters: Map<String, String>, clearParameters: Boolean, register: Boolean) {
        window.location.let { location ->
            // sets parameters
            val params = URLSearchParams(if (clearParameters) "" else location.search)
            parameters.forEach { params.set(it.key, it.value) }
            val currentPage = page ?: pages.find { it.id == location.pathname }

            // registers page to history if needed
            if (register) {
                val stringParams = params.toString().let { if (it.isNotBlank()) "?$it" else "" }
                val newUrl = "${location.protocol}//${location.host}${page?.id ?: "/"}$stringParams"
                window.history.pushState(null, "Centyllion ${currentPage?.titleKey?.let { i18n(it) }}", newUrl)
            }
        }
    }

}

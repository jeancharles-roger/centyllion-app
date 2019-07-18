package com.centyllion.client

import bulma.BulmaElement
import bulma.ElementColor
import bulma.Icon
import bulma.NavBar
import bulma.NavBarIconItem
import bulma.NavBarImageItem
import bulma.NavBarLinkItem
import bulma.Position
import bulmatoast.ToastAnimation
import bulmatoast.ToastOptions
import bulmatoast.bulmaToast
import com.centyllion.model.User
import keycloak.Keycloak
import keycloak.KeycloakInitOptions
import keycloak.KeycloakInstance
import kotlinx.io.IOException
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.url.URLSearchParams
import threejs.extra.core.Font
import threejs.loaders.FontLoader
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date
import kotlin.js.Promise

interface CssFile {
    val files: Array<String>
}

@JsName("index")
fun index() {
    // creates nav bar and adds it to body
    val navBar = NavBar(
        brand = listOf(
            NavBarImageItem(
                "https://www.centyllion.com/assets/images/logo-white-2by1.png", "/"
            ).apply { imgNode.style.maxHeight = "4rem" }
        ),
        end = listOf(
            NavBarIconItem(Icon("question-circle"), "https://centyllion.com/fr/documentation.html"),
            NavBarIconItem(Icon("envelope"), "mailto:bug@centyllion.com")
        ),
        transparent = true, color = ElementColor.Primary
    )
    document.body?.insertAdjacentElement(Position.AfterBegin.value, navBar.root)

    val root = document.querySelector(contentSelector) as HTMLElement

    // creates keycloak instance
    val keycloak = Keycloak()

    val api = Api(keycloak)
    api.addCss()

    val options = KeycloakInitOptions(
        promiseType = "native", onLoad = "check-sso", timeSkew = 10
    )
    keycloak.init(options)
        .then { _ -> api.fetchMe() }
        .then { user ->
            // creates context
            val context = BrowserContext(navBar, root, keycloak, user, api)

            // updates login link
            if (keycloak.tokenParsed != null) {
                val token = keycloak.tokenParsed.asDynamic()
                val userItem = NavBarLinkItem(
                    token.name as String? ?: token.preferred_username as String ?: "Not named",
                    keycloak.createAccountUrl()
                )
                val logoutItem = NavBarLinkItem("Logout", keycloak.createLogoutUrl(null))
                navBar.end += listOf(userItem, logoutItem)
            } else {
                val loginItem = NavBarLinkItem("Log In", keycloak.createLoginUrl(null))
                navBar.end += loginItem
            }

            // listens to pop state
            window.addEventListener("popstate", {
                findPageInUrl()?.let { context.openPage(it, register = false) }
            })

            // adds menu
            context.navBar.start = pages
                .filter { page -> page.header && page.authorized(context) }
                .map { page -> NavBarLinkItem(page.title, id = page.id) { context.openPage(page) } }

            // shows version
            showVersion(context.api)

            console.log("Starting function")

            val page = findPageInUrl() ?: if (user != null) homePage else explorePage
            context.openPage(page, register = false)
        }.catch {
            val context = BrowserContext(navBar, root, keycloak, null, api)
            context.error(it)
            console.error("Error on initialize")
            console.error(it.asDynamic().stack)
        }
}

/** Updates location with given [page] and [parameters]. It can also [register] the location to the history. */
fun updateLocation(page: Page<*>?, parameters: Map<String, String>, clearParameters: Boolean, register: Boolean) {
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

fun findPageInUrl(): Page<*>? {
    // gets params active page if any to activate it is allowed
    val params = URLSearchParams(window.location.search)
    return params.get("page")?.let { id -> pages.find { it.id == id } }
}

class BrowserContext(
    override val navBar: NavBar,
    override val root: HTMLElement,
    override val keycloak: KeycloakInstance,
    override val me: User?,
    override val api: Api = Api(keycloak)
) : AppContext {

    override val stripeKey = "pk_test_aPFW9HKhSHLmczJtGWW0Avdh00m1Ki47LU"

    private val fontCache: MutableMap<String, Font> = mutableMapOf()

    private var content: BulmaElement? = null
    private var currentPage: Page<BulmaElement>? = null

    private val storedEvents = mutableListOf<ClientEvent>()

    override val events: List<ClientEvent> get() = storedEvents

    /** Open the given [page] */
    override fun openPage(page: Page<*>, parameters: Map<String, String>, clearParameters: Boolean, register: Boolean) {

        val open = content?.let {
            currentPage?.exitCallback?.invoke(it, this)
        } ?: Promise.resolve(true)

        open.then {
            if (it) {
                currentPage = page.unsafeCast<Page<BulmaElement>>()

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

                // tries to load page if authorized
                if (page.authorized(this)) {
                    content = page.callback(this).also {
                        root.appendChild(it.root)
                    }
                } else {
                    error("You are not authorized to access this page")
                }
            }
        }
    }

    override fun getFont(path: String): Promise<Font> = Promise { resolve, reject ->
        val font = fontCache[path]
        if (font != null) resolve(font)

        FontLoader().load(api.url(path),
            { fontCache[path] = it; resolve(it) },
            {},
            { reject(IOException("${it.message}: ${it.filename}")) }
        )
    }

    override fun error(throwable: Throwable) {
        fun findCause(throwable: Throwable): Throwable = throwable.cause?.let { findCause(it) } ?: throwable
        findCause(throwable).let {
            error(it.message.toString())
            console.error(it)
        }
    }

    override fun error(content: String) = event(content, ElementColor.Danger)

    override fun warning(content: String) = event(content, ElementColor.Warning)

    override fun message(content: String) = event(content, ElementColor.Info)

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

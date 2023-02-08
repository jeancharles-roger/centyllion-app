package com.centyllion.client

import bulma.*
import com.centyllion.client.page.ShowPage
import com.centyllion.i18n.Locale
import com.centyllion.model.User
import com.centyllion.model.emptyGrainModelDescription
import com.centyllion.model.emptySimulationDescription
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

fun createNavBar(locale: Locale) = NavBar(
    brand = listOf(
        NavBarImageItem(
            "https://www.centyllion.com/assets/images/logo-white-2by1.png",
            "https://centyllion.com"
        ).apply { imgNode.style.maxHeight = "2rem" }
    ),
    end = listOf(),
    transparent = true, color = ElementColor.Primary
)

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

const val contentSelector = "section.cent-main"

fun main() = index()

fun index() {
    console.log("Starting app")

    // creates keycloak instance
    val api = Api()


    api.fetchLocales().then { locales ->
        val localeName = locales.resolve(window.navigator.language)
        console.log("Loading locale $localeName for ${window.navigator.language}")
        api.fetchLocale(localeName).then { locale ->

            val navBar = createNavBar(locale)
            document.body?.insertAdjacentElement(Position.AfterBegin.value, navBar.root)

            val user = User("root", "Me", "The Doctor")
            val context = BrowserContext(locale, navBar, user, api)
            val page = ShowPage(context)

            // adds add model button in navbar
            navBar.start += NavBarContentItem(
                Button(locale.i18n("Model"), Icon("plus"), ElementColor.Link, size = Size.Small) {
                    page.model = emptyGrainModelDescription
                    page.simulation = emptySimulationDescription
                }
            )

            navBar.end += NavBarIconItem(Icon("question-circle")) {
                page.startTutorial()
            }.apply {
                setTooltip(locale.i18n("Start tutorial"))
            }

            showInfo(context)
            showVersion(context.api)

            // replace
            val root = document.querySelector(contentSelector) as HTMLElement
            while (root.hasChildNodes()) root.removeChild(root.childNodes[0]!!)
            root.appendChild(page.root)
        }
    }
}

class BrowserContext(
    override val locale: Locale,
    override val navBar: NavBar,
    override val me: User?,
    override val api: Api = Api(),
    private val storedEvents: MutableList<ClientEvent> = mutableListOf()
) : AppContext {

    override val events: List<ClientEvent> get() = storedEvents

    override fun notify(event: ClientEvent) {
        storedEvents.add(event)
    }

}

package com.centyllion.client

import com.centyllion.client.page.ShowPage
import com.centyllion.i18n.Locale
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

const val contentSelector = ".cent-main"

fun main() = index()

fun index() {
    console.log("Starting app")

    // creates keycloak instance
    val api = Api()

    api.fetchLocales().then { locales ->
        val localeName = locales.resolve(window.navigator.language)
        console.log("Loading locale $localeName for ${window.navigator.language}")
        api.fetchLocale(localeName).then { locale ->

            val context = BrowserContext(locale, api)
            val page = ShowPage(context)
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
    override val api: Api = Api(),
) : AppContext
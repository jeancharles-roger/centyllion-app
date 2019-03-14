package com.centyllion.client

import Keycloak
import KeycloakInitOptions
import KeycloakInstance
import com.centyllion.client.controller.*
import com.centyllion.common.betaRole
import com.centyllion.common.centyllionHost
import com.centyllion.model.Simulator
import com.centyllion.model.sample.*
import kotlinx.html.a
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.h1
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.option
import kotlinx.html.select
import org.w3c.dom.*
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise


data class Page(
    val title: String,
    val id: String,
    val role: String,
    val callback: (root: HTMLElement, instance: KeycloakInstance) -> Unit
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = if (role != "") keycloak.hasRealmRole(role) else true
}

const val contentSelector = "section.cent-main"

val pages = listOf(
    Page("Model", "model", "", ::model),
    Page("Simulation", "simulation", "", ::simulation),
    Page("Profile", "profile", "", ::profile)
)

fun profile(root: HTMLElement, instance: KeycloakInstance) {

    val userController = UserController()
    val container = document.create.columns {
        column(ColumnSize(8), "cent-user")
    }
    container.querySelector(".cent-user")?.appendChild(userController.container)
    root.appendChild(container)

    // initialize controller
    fetchUser(instance).then { userController.data = it }

    // sets callbacks for update
    userController.onUpdate = { _, new, _ ->
        if (new != null) saveUser(new, instance) else null
    }

}

fun model(root: HTMLElement, instance: KeycloakInstance) {
}

fun simulation(root: HTMLElement, instance: KeycloakInstance) {
    val controller = SimulationController()
    val simulations = listOf(
        Simulator(dendriteModel(), dendriteSimulation(100, 100)),
        Simulator(dendriteModel(), dendriteSimulation(200, 200)),
        Simulator(bacteriaModel(), bacteriaSimulation(100, 100)),
        Simulator(bacteriaModel(), bacteriaSimulation(200, 200)),
        Simulator(immunityModel(), immunitySimulation(100, 100)),
        Simulator(immunityModel(), immunitySimulation(200, 200)),
        Simulator(carModel(), carSimulation(100, 100, 5)),
        Simulator(carModel(), carSimulation(200, 200, 5))
    )

    controller.data = simulations[0]

    root.appendChild(document.create.div("select") {
        select {
            simulations.forEach {
                option { +"${it.model.name} ${it.simulation.width}x${it.simulation.height}" }
            }
            onChangeFunction = {
                val target = it.target
                if (target is HTMLSelectElement) {
                    controller.data = simulations[target.selectedIndex]
                }
            }
        }
    })
    root.appendChild(controller.container)
}

@JsName("index")
fun index() {
    initialize().then { (instance, page) ->
        console.log("Starting function")
        if (page == null) {
            activatePage(pages.find { it.id == "simulation" }!!, instance)
        }
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

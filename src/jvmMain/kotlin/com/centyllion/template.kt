package com.centyllion

import kotlinx.html.*

@HtmlTagMarker
fun HTML.centyllionHead(title: String) =
    head {
        title { +title }
        meta("viewport", "width=device-width, initial-scale=1", "UTF-8")
        link("css/bulma.min.css", "stylesheet")
        link("css/centyllion.css", "stylesheet")

        script(src = "https://login.centyllion.com/auth/js/keycloak.js") {}
        script(src = "js/require.js") { }
        script(src = "js/centyllion/requirejs.config.json") { }
    }

@HtmlTagMarker
fun BODY.centyllionHeader() =
    section("section") {
        val navBarId = "mainNavBar"
        div("container") {
            nav("navbar is-transparent") {
                div("navbar-brand") {
                    a(href = "/", classes = "navbar-item ") {
                        img("Centyllion", "images/logo.png")
                    }
                    div("navbar-burger burger") {
                        attributes["data-target"] = navBarId
                        span { }
                        span { }
                        span { }
                    }
                }
                div("navbar-menu") {
                    id = navBarId
                    div("navbar-start")
                    div("navbar-end") {
                        div("navbar-item") {
                            div("field is-grouped") {
                                p("control") {
                                    span("tag is-primary") { id = "build"; +"Build" }
                                }
                                p("control") {
                                    span("tag is-info") { id = "date"; +"Date" }
                                }
                            }
                        }
                        a("/", classes = "cent-user navbar-item") { +"Not connected" }
                    }
                }
            }
        }
    }

@HtmlTagMarker
fun BODY.centyllionFooter() {
    footer("footer") {
        div("level") {
            div("level-item has-text-centered") {
                img("Centyllion", "images/logo.png") { width = "300px" }
                strong { +"platform" }
            }
        }
    }
}

@HtmlTagMarker
fun HTML.index() {
    centyllionHead("Centyllion")
    body {
        centyllionHeader()
        section("central cent-main") {
            div("container")
        }
        centyllionFooter()
    }
}

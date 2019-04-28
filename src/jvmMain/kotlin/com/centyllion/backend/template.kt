package com.centyllion.backend

import kotlinx.html.*

@HtmlTagMarker
fun HTML.centyllionHead(title: String) = head {
    title { +title }
    meta("viewport", "width=device-width, initial-scale=1", "UTF-8")
    link("css/centyllion-bulma.css", "stylesheet")
    link("css/centyllion.css", "stylesheet")
    link("https://use.fontawesome.com/releases/v5.7.2/css/all.css", "stylesheet") {
        integrity = "sha384-fnmOCqbTlWIlj8LyTjo7mOUStjsKC4pOpQbqyi7RrhN7udi9RwhKkMHpvLbHG9Sr"
        attributes["crossorigin"] = "anonymous"
    }

    script(src = "js/Keycloak-4.8.0/keycloak.js") {}
    script(src = "js/require.js") { }
    script(src = "js/centyllion/requirejs.config.json") { }
}

@HtmlTagMarker
fun BODY.centyllionFooter() {
    footer("footer") {
        div("level") {
            div("level-left") {
                p {
                    +"For any problem or question, contact "
                    a("mailto:bug@centyllion.com") { +"us" }
                }
            }
            div("level-item has-text-centered") {
                img("Centyllion", "images/logo-2by1.png") { width = "300px" }
                strong { +"platform" }
            }
            div("level-right") {
                // deployment info
                div("level-item field is-grouped") {
                    p("control") {
                        span("tag is-primary") { id = "build"; +"Build" }
                    }
                    p("control") {
                        span("tag is-info") { id = "date"; +"Date" }
                    }
                }
            }
        }
    }
}

@HtmlTagMarker
fun HTML.index() {
    centyllionHead("Centyllion")
    body {
        section("central cent-main") {
            div("container")
        }
        centyllionFooter()
    }
}

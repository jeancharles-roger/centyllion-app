package com.centyllion.backend

import kotlinx.html.*

@HtmlTagMarker
fun HTML.centyllionHead(title: String) = head {
    title { +title }
    meta("viewport", "width=device-width, initial-scale=1", "UTF-8")
    link("css/animate.css", "stylesheet")

    // Font Awesome
    link("https://use.fontawesome.com/releases/v5.8.1/css/solid.css", "stylesheet") {
        integrity = "sha384-QokYePQSOwpBDuhlHOsX0ymF6R/vLk/UQVz3WHa6wygxI5oGTmDTv8wahFOSspdm"
        attributes["crossorigin"] = "anonymous"
    }
    link("https://use.fontawesome.com/releases/v5.8.1/css/brands.css", "stylesheet") {
        integrity = "sha384-n9+6/aSqa9lBidZMRCQHTHKJscPq6NW4pCQBiMmHdUCvPN8ZOg2zJJTkC7WIezWv"
        attributes["crossorigin"] = "anonymous"
    }
    link("https://use.fontawesome.com/releases/v5.8.1/css/fontawesome.css", "stylesheet") {
        integrity = "sha384-vd1e11sR28tEK9YANUtpIOdjGW14pS87bUBuOIoBILVWLFnS+MCX9T6MMf0VdPGq"
        attributes["crossorigin"] = "anonymous"
    }

    // Javascript
    script(src = "js/bulma-toast-1.5.0/bulma-toast.min.js") {}
    script(src = "js/Keycloak-4.8.0/keycloak.js") {}
    script(src = "https://js.stripe.com/v3/") {}
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
                img("Centyllion", "https://centyllion.com/assets/images/logo-2by1.png") { width = "300px" }
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

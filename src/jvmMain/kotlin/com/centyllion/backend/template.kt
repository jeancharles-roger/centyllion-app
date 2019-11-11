package com.centyllion.backend

import kotlinx.html.BODY
import kotlinx.html.HTML
import kotlinx.html.HtmlTagMarker
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.section
import kotlinx.html.span
import kotlinx.html.title

@HtmlTagMarker
fun HTML.centyllionHead(
    title: String, description: String = "",
    image: String = "https://centyllion.com/assets/images/logo-square.png"
) = head {
    title { +title }
    meta("viewport", "width=device-width, initial-scale=1", "UTF-8")
    link("/css/animate.css", "stylesheet")

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
    script(src = "/js/require.js") {}

    script(src = "https://static.ekko.chat/now/ekkonow.min.js") {}

    /**
    <!-- Open graph -->
    <meta property="og:url" content="{{ site.url }}{{ page.url }}">
    <meta property="og:site_name" content="Centyllion">
    <meta property="article:published_time" content="2019-09-8 10:30:00 +0000" />
    <meta property="og:type" content="article">
    <meta property="og:locale" content="{{page.lang}}">
    <meta property="og:title" content="{{title}}">
    <meta property="og:description" content="{{ page.description }}">

    <meta property="og:image" content="{{ site.url }}{%if page.img %}{{ page.img }}{% else %}/assets/images/logo-2by1.png{% endif %}" />
    <meta property="article:publisher" content="https://twitter.com/centyllion" />
    <meta property="article:author" content="https://twitter.com/centyllion" />
    */

    // Twitter card
    meta("twitter:card", "summary")
    meta("twitter:title", title)
    meta("twitter:description", description)
    meta("twitter:image", image)
    meta("twitter:site", "@centyllion")
    meta("twitter:creator", "@centyllion")
    meta("twitter:domain", "centyllion.com")
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
                a {
                    href = "https://centyllion.com"
                    img("Centyllion", "https://centyllion.com/assets/images/logo-2by1.png") { width = "300px" }
                }
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
fun HTML.index(
    title: String = "Centyllion", description: String = "",
   image: String = "https://centyllion.com/assets/images/logo-square.png"
) {
    centyllionHead(title, description, image)
    body {
        section("central cent-main") {
            div("container")
        }
        centyllionFooter()
        script(src = "/js/centyllion/requirejs.config.json") {}
    }
}

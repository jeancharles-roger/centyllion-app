package com.centyllion.client

import bulma.BulmaElement
import kotlinx.html.dom.create
import kotlinx.html.js.a
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import kotlin.browser.document
import kotlin.browser.window

external fun encodeURIComponent(parameter: String): String

external fun <T> require(dependencies: Array<String>, block: (T) -> Unit)

// TODO Couldn't make it work with a val, recreate the renderer each time.
fun markdownToHtml(source: String) = window.asDynamic().markdownit().render(source) as String

fun Number.toFixed(size: Int = 3): String = toDouble().toFixed(size)
fun Double.toFixed(size: Int = 3): String = asDynamic().toFixed(size) as String

fun stringHref(content: String) = "data:text/plain;charset=utf-8,${encodeURIComponent(content)}"

fun blobHref(blob: Blob) = URL.createObjectURL(blob)

/** Download the given [content] with [filename] using the `<a>` trick. */
fun download(filename: String, href: String) {
    val a = document.create.a(href)
    a.setAttribute("download", filename)
    a.style.display = "none"

    document.body?.appendChild(a)
    a.click()
    document.body?.removeChild(a)
}

/** Toggles a element into fullscreen */
fun toggleElementToFullScreen(htmlElement: HTMLElement) {
    val d = document.asDynamic()
    if (d.webkitFullscreenElement != null || d.fullscreenElement != null) {
        if (d.webkitExitFullscreen != null) {
            d.webkitExitFullscreen()
        } else if (d.exitFullscreen != null) {
            d.exitFullscreen()
        }
    } else {
        val view = htmlElement.asDynamic()
        if (view.webkitRequestFullscreen != null) {
            view.webkitRequestFullscreen()
        } else if (view.requestFullscreen != null) {
            view.requestFullscreen()
        }
    }
    Unit
}

fun BulmaElement.setTooltip(text: String) {
    root.classList.add("has-tooltip")
    root.classList.add("has-tooltip-primary")
    root.classList.add("has-tooltip-bottom")
    root.setAttribute("data-tooltip", text)
}

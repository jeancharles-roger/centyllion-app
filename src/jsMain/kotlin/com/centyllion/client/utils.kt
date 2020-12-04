package com.centyllion.client

import bulma.BulmaElement
import com.centyllion.model.minFieldLevel
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.a
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob

external fun encodeURI(parameter: String): String
external fun encodeURIComponent(parameter: String): String

private val fieldFloatRegex = Regex("(0\\.(0*)([0-9]+))|(([1-9])\\.([0-9]+)e-([0-9]+))")

fun Float.toFieldString(): String = when {
    this < minFieldLevel -> ""
    this < 1e-3 -> {
        val output = this.toString()
        val match = fieldFloatRegex.matchEntire(output)
        when {
            match == null -> output
            output.startsWith("0") -> {
                val exponent = "-${match.groupValues[2].length+1}"
                val mantissa = match.groupValues[3].substring(0,1)
                "${mantissa}e${exponent}"
            }
            else -> {
                val exponent = "-${match.groupValues[7]}"
                val mantissa = match.groupValues[5]
                "${mantissa}e${exponent}"
            }
        }
    }
    else -> this.toFixed(3)
}

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
fun toggleElementToFullScreen(htmlElement: HTMLElement): Boolean {
    val d = document.asDynamic()
    if (d.webkitFullscreenElement != null || d.fullscreenElement != null) {
        if (d.webkitExitFullscreen != null) {
            d.webkitExitFullscreen()
        } else if (d.mozExitFullscreen != null) {
            d.mozExitFullscreen()
        } else if (d.exitFullscreen != null) {
            d.exitFullscreen()
        }
        return false
    } else {
        val view = htmlElement.asDynamic()
        if (view.webkitRequestFullscreen != null) {
            view.webkitRequestFullscreen()
        } else if (view.mozRequestFullscreen != null) {
            view.mozRequestFullscreen()
        } else if (view.requestFullscreen != null) {
            view.requestFullscreen()
        }
        return true
    }
}

fun BulmaElement.setTooltip(text: String) {
    root.classList.add("has-tooltip")
    root.classList.add("has-tooltip-primary")
    root.classList.add("has-tooltip-bottom")
    root.setAttribute("data-tooltip", text)
}

fun myHost() = encodeURI(document.location?.let { "${it.protocol}://${it.host}" } ?: "")
fun myUrl() = encodeURI(document.location?.toString() ?: "")

fun twitterHref(description: String) =
    "https://twitter.com/intent/tweet/?text=${encodeURI(description)}&url=${myUrl()}&via=centyllion"

fun facebookHref() = "https://facebook.com/sharer/sharer.php?u=${myUrl()}"

fun linkedInHref(description: String) =
    "https://www.linkedin.com/shareArticle?mini=true&url=${myUrl()}&title=${encodeURI(document.title)}&summary=${encodeURI(description)}&source=${myHost()}"

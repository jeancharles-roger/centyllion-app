package com.centyllion.client

import bulma.BulmaElement
import com.centyllion.model.ModelAndSimulation
import com.centyllion.model.minFieldLevel
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.dom.create
import kotlinx.html.js.a
import kotlinx.serialization.json.Json
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob

inline fun <reified T> Any?.takeInstance(): T? = if (this is T) this else null

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

private val markdownFlavour = CommonMarkFlavourDescriptor()
private val markdownParser = MarkdownParser(markdownFlavour)

fun renderMarkdown(source: String): String {
    val parsedTree = markdownParser.buildMarkdownTreeFromString(source)
    return HtmlGenerator(source, parsedTree, markdownFlavour).generateHtml()
}

fun loadFromStorage(): ModelAndSimulation? =
    window.localStorage.getItem("model")?.let {
        Json.decodeFromString(ModelAndSimulation.serializer(), it)
    }

fun saveToStorage(model: ModelAndSimulation) {
    window.setTimeout({
        val value = Json.encodeToString(ModelAndSimulation.serializer(), model)
        window.localStorage.setItem("model", value)
    }, 25)
}
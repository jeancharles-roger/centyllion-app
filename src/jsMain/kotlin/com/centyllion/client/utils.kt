package com.centyllion.client

import bulmatoast.BulmaToast
import kotlinx.html.dom.create
import kotlinx.html.js.a
import stripe.Stripe
import kotlin.browser.document

external fun encodeURIComponent(parameter: String): String

external fun <T> require(dependencies: Array<String>, block: (T) -> Unit)

fun runWithStripe(key: String, block: (Stripe) -> Unit) =
    require<(String) -> Stripe>(arrayOf("stripe")) { block(it(key)) }

fun Double.toFixed(size: Int = 3): String = asDynamic().toFixed(size) as String

@JsName("dependencies")
fun dependencies(bulmaToast: BulmaToast) {
    bulmatoast.bulmaToast = bulmaToast
}

/** Download the given [content] with [filename] using the `<a>` trick. */
fun download(filename: String, content: String) {
    val href = "data:text/plain;charset=utf-8,${encodeURIComponent(content)}"
    val a = document.create.a(href)
    a.setAttribute("download", filename)
    a.style.display = "none"

    document.body?.appendChild(a)
    a.click()
    document.body?.removeChild(a)
}

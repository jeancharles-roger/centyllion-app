package com.centyllion.client

import bulmatoast.BulmaToast
import stripe.Stripe

external fun encodeURIComponent(parameter: String): String

external fun <T> require(dependencies: Array<String>, block: (T) -> Unit)

fun runWithStripe(key: String, block: (Stripe) -> Unit) =
    require<(String) -> Stripe>(arrayOf("stripe")) { block(it(key)) }

fun Double.toFixed(size: Int = 3): String = asDynamic().toFixed(size) as String

@JsName("dependencies")
fun dependencies(bulmaToast: BulmaToast) {
    bulmatoast.bulmaToast = bulmaToast
}

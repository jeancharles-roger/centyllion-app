package com.centyllion.client

import stripe.Stripe

external fun <T> require(dependencies: Array<String>, block: (T) -> Unit)

fun runWithStripe(key: String, block: (Stripe) -> Unit) =
    require<(String) -> Stripe>(arrayOf("stripe")) { block(it(key)) }

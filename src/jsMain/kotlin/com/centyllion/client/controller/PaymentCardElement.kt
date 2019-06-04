package com.centyllion.client.controller

import bulma.BulmaElement
import bulma.ElementColor
import bulma.textButton
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.label
import kotlinx.html.role
import org.w3c.dom.HTMLElement
import stripe.Stripe
import stripe.Token
import stripe.elements.ElementsType
import kotlin.browser.document

class PaymentCardElement(stripeKey: String, onPay: (Token) -> Unit): BulmaElement {

    val stripe = Stripe(stripeKey)

    val elements = stripe.elements()
    val card = elements.create(ElementsType.card.name)

    val button = textButton("Pay", ElementColor.Primary, rounded = true) { button ->
        stripe.createToken(card).then { response ->
            errorNode.innerText = response.error?.message ?: ""
            response.token?.let {
                button.disabled = true
                onPay(it)
            }
        }
    }

    override val root: HTMLElement = document.create.div {
        id = "paymentForm"
        div("form-row") {
            label {
                htmlFor = "form-row"
                +"Credit or debit card"
            }
            div { id = "card-element" }
            div { id = "card-errors"; role = "alert" }
        }
    }

    val cardNode = root.querySelector("#card-element") as HTMLElement
    val errorNode = root.querySelector("#card-errors") as HTMLElement

    init {
        root.appendChild(button.root)
        card.mount(cardNode)
    }
}

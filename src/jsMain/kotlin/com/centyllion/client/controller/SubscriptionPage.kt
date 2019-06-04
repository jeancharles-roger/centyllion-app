package com.centyllion.client.controller

import bulma.BulmaElement
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import com.centyllion.client.AppContext

class SubscriptionPage(context: AppContext) : BulmaElement {

    val payment = PaymentCardElement(context.stripeKey) { token ->
        console.log(token)
    }

    val container = Columns(
        Column(payment, size = ColumnSize.OneThird)
    )

    override val root = container.root

}

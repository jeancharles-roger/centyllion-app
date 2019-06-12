package com.centyllion.client.page

import bulma.BulmaElement
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import com.centyllion.client.AppContext
import com.centyllion.client.controller.navigation.PaymentCardElement

class SubscriptionPage(context: AppContext) : BulmaElement {

    val payment = PaymentCardElement(context.stripeKey) { token ->
        console.log(token)
    }

    val container = Columns(
        Column(payment, size = ColumnSize.OneThird)
    )

    override val root = container.root

}

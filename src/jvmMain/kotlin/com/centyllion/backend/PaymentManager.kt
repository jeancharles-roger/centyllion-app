package com.centyllion.backend

import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.checkout.SessionCreateParams.SubscriptionData

interface PaymentManager

class StripePaymentManager(val stripeKey: String) : PaymentManager {

    val successUrl = "/api/subscription/success"
    val cancelUrl = "/api/subscription/cancel"

    val creatorPlanId = "prod_FAEliXJiTym0YQ"

    init {
        // Set your secret key: remember to change this to your live secret key in production
        // See your keys here: https://dashboard.stripe.com/account/apikeys
        Stripe.apiKey = "sk_test_HkErg109kgJGEiAAHK4WY4w300JAOVXFje"
    }

    fun test() {


        val subscriptionItem = SubscriptionData.Item.builder()
            .setPlan(creatorPlanId).build()

        val subscriptionData = SubscriptionData.builder()
            .addItem(subscriptionItem).build()

        val sessionParams = SessionCreateParams.builder()
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setSubscriptionData(subscriptionData)
            .setSuccessUrl(successUrl)
            .setCancelUrl(cancelUrl).build()

        val session = Session.create(sessionParams)
    }

}


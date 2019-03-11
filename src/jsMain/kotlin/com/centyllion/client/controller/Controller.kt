package com.centyllion.client.controller

import org.w3c.dom.HTMLElement

interface Controller<Data> {

    var data: Data

    val container: HTMLElement

    fun refresh()
}

package com.centyllion.client

import com.centyllion.common.Behaviour
import kotlinx.html.dom.create
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import kotlin.browser.document

class BehaviourController: Controller<Behaviour> {

    override var data: Behaviour = Behaviour()
        set(value) {
            if (value != field) {
                field = value
                refresh()
            }
        }

    override val container: HTMLElement = document.create.div {
    }

    override fun refresh() {
        container.innerText = data.toString()
    }

}

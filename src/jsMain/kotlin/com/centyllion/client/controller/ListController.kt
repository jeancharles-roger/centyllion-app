package com.centyllion.client.controller

import kotlinx.html.dom.create
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.properties.Delegates.observable

class ListController<Data>(
    initialList: List<Data>,
    extraColumnsClasses: String = "",
    val columnSize: ColumnSize = size(4),
    val controllerBuilder: (Int, Data) -> Controller<Data>
): Controller<List<Data>> {

    override var data: List<Data> by observable(initialList) { _, _, _ -> refresh() }

    private var internalControllers: MutableList<Controller<Data>> = mutableListOf()

    val dataControllers: List<Controller<Data>> get() = internalControllers

    override val container: HTMLElement = document.create.div("columns is-multiline $extraColumnsClasses")

    init {
        refresh()
    }

    // TODO implements controller re-use
    override fun refresh() {
        container.innerHTML = ""

        internalControllers.clear()
        data.forEachIndexed { index, data ->
            val controller = controllerBuilder(index, data)
            internalControllers.add(controller)
            container.appendChild(column(controller.container, columnSize))
        }
    }
}

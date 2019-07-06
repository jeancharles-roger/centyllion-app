package com.centyllion.client.controller.navigation

import bulma.BulmaElement
import bulma.Column
import bulma.Controller
import bulma.Div
import bulma.NoContextController
import bulma.Pagination
import bulma.PaginationAction
import bulma.PaginationLink
import bulma.noContextColumnsController
import com.centyllion.model.ResultPage
import com.centyllion.model.emptyResultPage
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class ResultPageController<Data, Ctrl : Controller<Data, Unit, Column>>(
    controllerBuilder: (data: Data, previous: Ctrl?) -> Ctrl,
    var fetch: (offset: Int, Limit: Int) -> Promise<ResultPage<Data>> = { _, _ -> Promise.resolve(emptyResultPage()) },
    var onClick: (Data, Ctrl) -> Unit = { _, _ -> },
    var error: (Throwable) -> Unit = {}
) : NoContextController<ResultPage<Data>, BulmaElement>() {

    override var data: ResultPage<Data> by observable(emptyResultPage()) { _, old, new ->
        if (old != new) {
            contentController.data = new.content
            refresh()
        }
    }

    override var readOnly: Boolean = true

    var limit: Int by observable(16) { _, old, new ->
        if (old != new) fetch(offset, new).then { data = it }.catch { error(it) }
    }

    var offset: Int by observable(0) { _, old, new ->
        if (old != new) fetch(new, limit).then { data = it }.catch { error(it) }
    }

    val next = PaginationAction("Next") { offset += limit }

    val previous = PaginationAction("Previous") { offset -= limit }

    val pagination = Pagination(previous = previous, next = next, rounded = true)

    val contentController =
        noContextColumnsController(data.content, controllerBuilder = controllerBuilder)
            .apply { this.onClick = this@ResultPageController.onClick }

    override val container: BulmaElement = Div(pagination, contentController)

    fun refreshFetch() = fetch(offset, limit).then { data = it }.catch { error(it) }

    override fun refresh() {
        pagination.items = (0..(data.totalSize - 1) / limit).map { page ->
            val pageOffset = page * limit
            PaginationLink("$page", current = (pageOffset == data.offset)) { offset = pageOffset }
        }
        previous.disabled = offset == 0
        next.disabled = offset > (data.totalSize-1) - limit
    }

}

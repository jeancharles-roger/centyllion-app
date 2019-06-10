package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.ResultPage
import com.centyllion.model.emptyResultPage
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class ResultPageController<Data, Ctrl : Controller<Data, Unit, Column>>(
    controllerBuilder: (MultipleController<Data, Unit, Columns, Column, Ctrl>, data: Data, previous: Ctrl?) -> Ctrl,
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

    var limit: Int by observable(8) { _, old, new ->
        if (old != new) refresh()
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

    override val container: BulmaElement = div(pagination, contentController)

    override fun refresh() {
        pagination.items = (0..data.totalSize / limit).map { page ->
            val pageOffset = page * limit
            PaginationLink("$page", current = (pageOffset == data.offset)) { offset = pageOffset }
        }
        previous.disabled = offset == 0
        next.disabled = offset > data.totalSize - limit
    }

}

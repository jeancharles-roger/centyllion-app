package com.centyllion.client.controller.navigation

import bulma.BulmaElement
import bulma.Controller
import bulma.MultipleController
import bulma.NoContextController
import bulma.Pagination
import bulma.PaginationAction
import bulma.PaginationLink
import com.centyllion.i18n.Locale
import com.centyllion.model.ResultPage
import com.centyllion.model.emptyResultPage
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class ResultPageController<
    /** Data type for each controller */
    Data,
    /** Context for each controller (on context for all) */
    Context,
    /** BulmaElement for the whole list */
    ParentElement : BulmaElement,
    /** BulmaElement for each item */
    ItemElement : BulmaElement,
    /** Controller for each item */
    Ctrl : Controller<Data, Context, ItemElement>
>(
    locale: Locale,
    val contentController: MultipleController<Data, Context, ParentElement, ItemElement, Ctrl>,
    val toHeader: (Pagination) -> ItemElement,
    var fetch: (offset: Int, Limit: Int) -> Promise<ResultPage<Data>> = { _, _ -> Promise.resolve(emptyResultPage()) },
    var error: (Throwable) -> Unit = {},
    initialLimit: Int = 16
) : NoContextController<ResultPage<Data>, ParentElement>() {

    override var data: ResultPage<Data> by observable(emptyResultPage()) { _, old, new ->
        if (old != new) {
            contentController.data = new.content
            refresh()
        }
    }

    override var readOnly: Boolean = true

    var limit: Int by observable(initialLimit) { _, old, new ->
        if (old != new) fetch(offset, new).then { data = it }.catch { error(it) }
    }

    var offset: Int by observable(0) { _, old, new ->
        if (old != new) fetch(new, limit).then { data = it }.catch { error(it) }
    }

    val next = PaginationAction(locale.i18n("Next")) { offset += limit }

    val previous = PaginationAction(locale.i18n("Previous")) { offset -= limit }

    val pagination = Pagination(previous = previous, next = next, rounded = true).apply {
        contentController.header += toHeader(this)
    }

    override val container = contentController.container

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

package com.centyllion.client.controller.utils

import bulma.*
import com.centyllion.client.page.BulmaPage
import kotlin.properties.Delegates.observable

class SearchController(
    page: BulmaPage, search: String = "", onUpdate: (SearchController, String) -> Unit = { _, _ -> }
): NoContextController<String, Field>() {

    override var data: String by observable(search) { _, old, new ->
        if (old != new) {
            searchInput.value = new
            onUpdate(this, new)
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        this.searchInput.disabled = new
        this.clearSearch.disabled = new
    }

    // search input
    val searchInput: Input = Input(search, page.i18n("Search"), rounded = true, size = Size.Small) { _, v ->
        data = v
    }

    val clearSearch = iconButton(Icon("times"), rounded = true, size = Size.Small) { data = "" }

    override val container = Field(Control(searchInput, Icon("search"), expanded = true), Control(clearSearch), addons = true)

    override fun refresh() { }
}

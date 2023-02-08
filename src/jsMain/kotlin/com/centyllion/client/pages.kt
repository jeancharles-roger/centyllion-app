package com.centyllion.client

import com.centyllion.client.page.BulmaPage
import com.centyllion.client.page.ShowPage

data class Page(
    val titleKey: String, val id: String,
    val header: Boolean, val callback: (appContext: AppContext) -> BulmaPage
) {
    fun authorized(context: AppContext): Boolean = true
}

const val contentSelector = "section.cent-main"
val showPage = Page("Show", "/", false, ::ShowPage)

val pages = listOf(showPage)


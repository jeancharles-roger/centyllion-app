package com.centyllion.client.tutorial

import bulma.BulmaElement
import com.centyllion.client.page.BulmaPage
import org.w3c.dom.HTMLElement

data class TutorialStep(
    val title: String,
    val content: List<BulmaElement>,
    val selector: (BulmaPage) -> HTMLElement
)

data class Tutorial(
    val name: String,
    val steps: List<TutorialStep>
) {
    fun isNotEmpty() = steps.isNotEmpty()
}

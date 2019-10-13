package com.centyllion.client.tutorial

import bulma.BulmaElement
import com.centyllion.client.page.BulmaPage
import org.w3c.dom.HTMLElement

data class TutorialStep<P: BulmaPage>(
    val title: String,
    val content: List<BulmaElement>,
    val selector: (P) -> HTMLElement,
    val validated: (P) -> Boolean
)

data class Tutorial<P: BulmaPage>(
    val name: String,
    val steps: List<TutorialStep<P>>
) {
    fun isNotEmpty() = steps.isNotEmpty()
}

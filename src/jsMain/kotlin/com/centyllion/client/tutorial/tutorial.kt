package com.centyllion.client.tutorial

import bulma.BulmaElement
import com.centyllion.client.page.BulmaPage
import org.w3c.dom.HTMLElement

class TutorialStep<P: BulmaPage>(
    val title: String,
    val content: List<BulmaElement>,
    val selector: (P) -> HTMLElement,
    val validated: (P) -> Boolean,
    val nextCallback: (P) -> Unit = { }
)

interface Tutorial<P: BulmaPage> {
    val name: String

    val steps: List<TutorialStep<P>>

    fun isNotEmpty() = steps.isNotEmpty()
}

class SimpleTutorial<P: BulmaPage>(
    override val name: String,
    override val steps: List<TutorialStep<P>>
): Tutorial<P>

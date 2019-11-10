package com.centyllion.client.tutorial

import bulma.BulmaElement
import com.centyllion.client.page.BulmaPage
import org.w3c.dom.HTMLElement

class TutorialStep(
    val title: String,
    val content: List<BulmaElement>,
    val selector: () -> HTMLElement,
    val validated: () -> Boolean,
    val nextCallback: () -> Unit = { }
)

interface Tutorial<P: BulmaPage> {
    val page: P

    val name: String

    val introduction: List<BulmaElement>

    val steps: List<TutorialStep>

    val conclusion: List<BulmaElement>

    val next: Tutorial<P>?

    fun isNotEmpty() = steps.isNotEmpty()

    fun i18n(key: String, vararg parameters: Any): String = page.i18n(key, parameters)
}

class SimpleTutorial<P: BulmaPage>(
    override val page: P,
    override val name: String,
    override val steps: List<TutorialStep>,
    override val introduction: List<BulmaElement> = emptyList(),
    override val conclusion: List<BulmaElement> = emptyList(),
    override val next: Tutorial<P>? = null
): Tutorial<P>

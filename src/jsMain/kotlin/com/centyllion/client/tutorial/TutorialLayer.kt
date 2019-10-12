package com.centyllion.client.tutorial

import bulma.Delete
import bulma.Div
import bulma.ElementColor
import bulma.Icon
import bulma.Level
import bulma.Message
import bulma.iconButton
import bulma.p
import bulma.span
import com.centyllion.client.page.BulmaPage
import kotlin.browser.document
import kotlin.browser.window

class TutorialLayer(
    val page: BulmaPage,
    val tutorial: Tutorial
) {
    init { require(tutorial.isNotEmpty())}

    var currentStep = 0

    private fun placeForStep(step: TutorialStep) {
        val target = step.selector(page)
        val bodyBox = document.body?.getBoundingClientRect()
        val elementBox = target.getBoundingClientRect()

        val top = (elementBox.top - (bodyBox?.top ?: 0.0) + elementBox.height)
        container.root.style.top = "${top}px"

        val left = elementBox.left - (bodyBox?.left ?: 0.0) + (target.clientWidth - container.root.clientWidth) / 2
        val coercedLeft = left.coerceIn(bodyBox?.left ?: 0.0, (bodyBox?.width ?: Double.MAX_VALUE) - container.root.clientWidth)
        container.root.style.left = "${coercedLeft}px"
    }

    private fun setStep() {
        val step = tutorial.steps[currentStep]
        title.text = step.title
        content.body = step.content
        placeForStep(step)

        stepDots.forEachIndexed { i, e ->
            e.root.classList.toggle("has-background-success", i == currentStep)
            e.root.classList.toggle("has-background-grey-light", i != currentStep)
        }

        previousButton.disabled = currentStep == 0
        nextButton.disabled = currentStep >= tutorial.steps.size - 1
    }

    fun start() {
        document.body?.appendChild(container.root)
        container.root.classList.add("fadeIn")
        setStep()
    }

    fun previous() {
        currentStep += -1
        setStep()
    }

    fun next() {
        currentStep += 1
        setStep()
    }

    fun stop() {
        container.root.classList.remove("fadeIn")
        container.root.classList.add("fadeOut")
        window.setTimeout( { document.body?.removeChild(container.root) }, 1000)
        currentStep = 0
    }

    val title = p("")
    val delete = Delete { stop() }

    val content = Div().apply { root.style.marginBottom = "1rem" }

    val previousButton = iconButton(Icon("arrow-left"), ElementColor.Success, rounded = true) { previous() }
    val nextButton = iconButton(Icon("arrow-right"), ElementColor.Success, rounded = true) { next() }
    val stepDots = tutorial.steps.mapIndexed { i, _ ->
        span(classes = "circle ${if (i == currentStep) "has-background-success" else "has-background-grey-light"}")
    }
    val footBar = Level(
        left = listOf(previousButton),
        center = listOf(Div().apply { body = stepDots }),
        right = listOf(nextButton)
    )

    val container = Message(
        header = listOf(title, delete),
        body = listOf(content, footBar),
        color = ElementColor.Success
    ).apply {
        root.classList.add("tutorial")
        root.classList.add("animated")
    }
}


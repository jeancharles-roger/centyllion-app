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
import bulma.textButton
import com.centyllion.client.page.BulmaPage
import kotlin.browser.document
import kotlin.browser.window

class TutorialLayer<P: BulmaPage>(val tutorial: Tutorial<P>) {
    init { require(tutorial.isNotEmpty())}

    var started = false
    var currentStep = 0

    private fun placeStep(step: TutorialStep) {
        val target = step.selector()
        val bodyBox = document.body?.getBoundingClientRect()
        val elementBox = target.getBoundingClientRect()

        // places help content
        val top = (elementBox.top - (bodyBox?.top ?: 0.0) + elementBox.height)
        container.root.style.top = "${top}px"

        val left = elementBox.left - (bodyBox?.left ?: 0.0) + (target.clientWidth - container.root.clientWidth) / 2
        val coercedLeft = left.coerceIn(bodyBox?.left ?: 0.0, (bodyBox?.width ?: Double.MAX_VALUE) - container.root.clientWidth)
        container.root.style.left = "${coercedLeft}px"

        arrow.root.style.top = "${elementBox.top - (bodyBox?.top ?: 0.0) + elementBox.height}px"
        arrow.root.style.left = "${elementBox.left - (bodyBox?.left ?: 0.0) + elementBox.width/2.0}px"
    }

    private fun setStep() {
        val step = tutorial.steps[currentStep]
        title.text = step.title
        content.body = step.content
        placeStep(step)

        stepDots.forEachIndexed { i, e ->
            e.root.classList.toggle("has-background-success", i == currentStep)
            e.root.classList.toggle("has-background-grey-light", i != currentStep)
        }

        previousButton.disabled = currentStep == 0
        nextButton.disabled = !step.validated() || currentStep >= tutorial.steps.size - 1
    }

    private fun checking() {
        val step = tutorial.steps[currentStep]
        if (step.validated()) next()
        if (started) window.setTimeout(this::checking, 250)
    }

    fun start() {
        // first open the introduction if there is one
        if (tutorial.introduction.isNotEmpty()) {
            tutorial.page.modalDialog(
                tutorial.i18n("Tutorial '%0'", tutorial.i18n(tutorial.name)), tutorial.introduction,
                textButton(tutorial.i18n("Start tutorial"), color = ElementColor.Success) { startSteps() },
                textButton(tutorial.i18n("Ok but later")) {  },
                textButton(tutorial.i18n("I don't need it"), color = ElementColor.Warning) {  }
            )
        } else {
            startSteps()
        }
    }

    private fun startSteps() {
        started = true
        window.setTimeout(this::checking, 1000)

        document.body?.appendChild(arrow.root)
        document.body?.appendChild(container.root)

        arrow.root.classList.add("fadeIn")
        container.root.classList.add("fadeIn")
        setStep()
    }

    fun previous() {
        if (currentStep > 0) {
            currentStep -= 1
            setStep()
        }
    }

    fun next() {
        when {
            currentStep < tutorial.steps.size - 1 -> {
                tutorial.steps[currentStep].nextCallback()
                currentStep += 1
                setStep()
            }
            currentStep == tutorial.steps.size - 1 -> {
                tutorial.steps[currentStep].nextCallback()
                stop()
            }
        }
    }

    fun stop() {
        started = false

        arrow.root.classList.remove("fadeIn")
        container.root.classList.remove("fadeIn")

        arrow.root.classList.add("fadeOut")
        container.root.classList.add("fadeOut")

        window.setTimeout( {
            document.body?.removeChild(arrow.root)
            document.body?.removeChild(container.root)
        }, 1000)

        // opens the conclusion if there is one and tutorial was done all the way
        if (currentStep == tutorial.steps.size - 1 && tutorial.conclusion.isNotEmpty()) {
            tutorial.page.modalDialog(
                tutorial.name, tutorial.conclusion,
                textButton(tutorial.i18n("Ok"), color = ElementColor.Success) { stop() }
            )
        }

        currentStep = 0
    }

    val arrow = span(classes = "tutorial tutorial-arrow animated")
    val title = p("")
    val delete = Delete { stop() }

    val content = Div().apply {
        root.style.marginBottom = "1rem"
    }

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
        root.classList.add("tutorial-content")
        root.classList.add("animated")
    }
}


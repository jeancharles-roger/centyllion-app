package com.centyllion.client.tutorial

import bulma.*
import com.centyllion.client.page.BulmaPage
import kotlinx.browser.document
import kotlinx.browser.window

class TutorialLayer<P: BulmaPage>(
    initial: Tutorial<P>, val onEnd: (TutorialLayer<P>) -> Unit = {}
 ) {

    var tutorial = initial

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
        val maximumLeft = ((bodyBox?.width ?: Double.MAX_VALUE) - container.root.clientWidth).coerceAtLeast(0.0)
        val coercedLeft = left.coerceIn(bodyBox?.left ?: 0.0, maximumLeft)
        container.root.style.left = "${coercedLeft}px"

        arrow.root.style.top = "${elementBox.top - (bodyBox?.top ?: 0.0) + elementBox.height}px"
        arrow.root.style.left = "${elementBox.left - (bodyBox?.left ?: 0.0) + elementBox.width/2.0}px"
    }

    private fun setStep() {
        val step = tutorial.steps[currentStep]
        title.text = step.title
        content.body = step.content
        progression.value = currentStep
        placeStep(step)
    }

    private fun checking() {
        if (currentStep < tutorial.steps.size) {
            val step = tutorial.steps[currentStep]
            if (step.validated()) next()
        }
        if (started) window.setTimeout(this::checking, 250)
    }

    fun start() {
        // creates a modal dialog to present the tutorial
        tutorial.page.modalDialog(
            tutorial.i18n("Tutorial '%0'", tutorial.i18n(tutorial.name)), tutorial.introduction,
            textButton(tutorial.i18n("Start tutorial"), color = ElementColor.Success) {
                startSteps()
            },
            textButton(tutorial.i18n("Ok but later")) { /* nothing to do */ },
        )
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

    fun next() {
        tutorial.steps[currentStep].nextCallback()
        currentStep += 1
        when {
            currentStep < tutorial.steps.size -> setStep()
            currentStep == tutorial.steps.size -> lastStep()
        }
    }

    fun lastStep() {
        // opens the conclusion if there is one and tutorial was done all the way
        if (tutorial.conclusion.isNotEmpty()) {
            val buttons = listOfNotNull(
                textButton(tutorial.i18n("Ok"), color = ElementColor.Success) { stop() },
                tutorial.next?.let { next ->
                    textButton(tutorial.i18n(next.name), color = ElementColor.Primary) {
                        tutorial = next
                        currentStep = 0
                        start()
                    }
                }
            ).toTypedArray()

            tutorial.page.modalDialog(tutorial.i18n(tutorial.name), tutorial.conclusion, *buttons)
        } else {
            stop()
        }
    }


    fun stop() {
        started = false

        arrow.root.classList.remove("fadeIn")
        container.root.classList.remove("fadeIn")

        arrow.root.classList.add("fadeOut")
        container.root.classList.add("fadeOut")

        window.setTimeout( {
            document.body?.let { body ->
                if (body.contains(arrow.root)) body.removeChild(arrow.root)
                if (body.contains(container.root)) body.removeChild(container.root)
            }
        }, 1000)

        currentStep = 0
        onEnd(this)
    }

    val arrow = span(classes = "tutorial tutorial-arrow animated")
    val title = p("")
    val delete = Delete { stop() }

    val content = Div().apply {
        root.style.marginBottom = "1rem"
    }

    val progression = ProgressBar().apply {
        color = ElementColor.Success
        min = 0
        value = currentStep
        max = tutorial.steps.size - 1
    }

    val container = Message(
        header = listOf(title, delete),
        body = listOf(content, progression),
        color = ElementColor.Success
    ).apply {
        root.classList.add("tutorial")
        root.classList.add("tutorial-content")
        root.classList.add("animated")
    }
}


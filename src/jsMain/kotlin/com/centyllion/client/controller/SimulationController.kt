package com.centyllion.client.controller

import chartjs.*
import com.centyllion.client.emptyModelAndSimulation
import com.centyllion.model.Simulator
import kotlinx.html.a
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.h2
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.browser.window

class SimulationController : Controller<Simulator> {

    override var data: Simulator = Simulator(emptyModelAndSimulation())
        set(value) {
            if (value != field) {
                field = value
                grainsController.data = value.model.grains
                behaviourController.data = value.model.behaviours
                running = false
                refresh()
                refreshChart()
            }
        }

    inline val model get() = data.model

    inline val simulation get() = data.simulation

    var running = false
    var lastRefresh = 0

    val grainsController = ListController(
        model.grains, "", size(12)
    ) { _, grain ->
        GrainDisplayController().apply { data = grain }
    }

    val behaviourController = ListController(
        model.behaviours, "", size(12)
    ) { _, behaviour ->
        BehaviourDisplayController().apply { data = behaviour }
    }

    override val container: HTMLElement = document.create.div {
        columns {
            column(size(8, centered = true)) {
                div("level") {
                    div("level-left buttons") {
                        a(classes = "button is-rounded is-primary cent-run") {
                            +"Run"
                            onClickFunction = {
                                run()
                            }
                        }
                        a(classes = "button is-rounded is-info cent-step") {
                            +"Step"
                            onClickFunction = {
                                step()
                            }
                        }
                        a(classes = "button is-rounded is-danger cent-stop") {
                            +"Stop"
                            onClickFunction = {
                                stop()
                            }
                        }
                        a(classes = "button is-rounded is-warning cent-reset") {
                            +"Reset"
                            onClickFunction = {
                                reset()
                            }
                        }
                    }
                    div("level-item cent-stepcount") {
                        span("label")
                    }
                }
                div("has-text-centered") {
                    canvas("cent-rendering") {
                        val canvasWidth = (window.innerWidth - 20).coerceAtMost(600)
                        width = "$canvasWidth"
                        height = "${simulation.height * canvasWidth / simulation.width}"
                    }
                }
            }
            column(size(4)) {
                h2("subtitle") { +"Grains"}
                div( "cent-grains")
                h2("subtitle") { +"Behaviors"}
                div("cent-behaviors")
            }
        }
        columns("is-centered") {
            column(size(10)) {
                canvas("cent-graph") {}
            }
        }
    }

    val runButton = container.querySelector("a.cent-run") as HTMLAnchorElement
    val stepButton = container.querySelector("a.cent-step") as HTMLAnchorElement
    val stopButton = container.querySelector("a.cent-stop") as HTMLAnchorElement
    val resetButton = container.querySelector("a.cent-reset") as HTMLAnchorElement

    val stepCount = container.querySelector(".cent-stepcount > span") as HTMLSpanElement
    val canvas = container.querySelector(".cent-rendering") as HTMLCanvasElement

    val context = canvas.getContext("2d") as CanvasRenderingContext2D

    val graphCanvas = container.querySelector("canvas.cent-graph") as HTMLCanvasElement

    val chart = Chart(graphCanvas, LineChartConfig(
        options = LineChartOptions().apply {
            animation.duration = 0
            scales.xAxes = arrayOf(LinearAxisOptions())
            scales.yAxes = arrayOf(LinearAxisOptions())
        }
    ))

    init {
        container.querySelector("div.cent-grains")?.appendChild(grainsController.container)
        container.querySelector("div.cent-behaviors")?.appendChild(behaviourController.container)

        refresh()
    }

    fun run() {
        if (!running) {
            running = true
            runningCallback()
        }
    }

    fun runningCallback() {
        if (running) {
            executeStep(lastRefresh >= 5)
            lastRefresh += 1
            window.setTimeout(this::runningCallback, 0)
        }
    }

    fun step() {
        if (!running) {
            executeStep(true)
        }
    }

    fun stop() {
        if (running) {
            running = false
            refresh()
        }
    }

    fun reset() {
        if (!running) {
            data.reset()
            refresh()
            refreshChart()
        }
    }

    private fun executeStep(update: Boolean) {
        data.oneStep()
        refresh()

        val counts = data.lastGrainsCount().values
        chart.data.datasets.zip(counts) { set: LineDataSet, i: Int ->
            val data = set.data
            if (data != null) {
                val index = if (data.isEmpty()) 0 else data.lastIndex
                data.push(LineChartPlot(index, i))
            }
        }
        grainsController.dataControllers.zip(counts) { controller, count ->
            if (controller is GrainDisplayController) {
                controller.count = count
            }
        }
        if (update) {
            chart.update(ChartUpdateConfig(duration = 0, lazy = true))
            lastRefresh = 0
        }
    }

    override fun refresh() {

        runButton.classList.toggle("is-loading", running)
        if (running) {
            runButton.setAttribute("disabled", "")
            stepButton.setAttribute("disabled", "")
            stopButton.removeAttribute("disabled")
            resetButton.setAttribute("disabled", "")
        } else {
            runButton.removeAttribute("disabled")
            stepButton.removeAttribute("disabled")
            stopButton.setAttribute("disabled", "")
            resetButton.removeAttribute("disabled")
        }

        stepCount.innerText = "${data.step}"


        // refreshes simulation view
        val canvasWidth = canvas.width.toDouble()
        val canvasHeight = canvas.height.toDouble()
        val xSize = canvasWidth / simulation.width
        val xMax = simulation.width * xSize
        val ySize = canvasHeight / simulation.height
        context.clearRect(0.0, 0.0, canvasWidth, canvasHeight)
        var currentX = 0.0
        var currentY = 0.0
        for (i in 0 until simulation.agents.size) {
            val grain = simulation.grainAtIndex(i)
            if (grain != null) {
                context.fillStyle = grain.color
                context.fillRect(currentX, currentY, xSize, ySize)
            }

            currentX += xSize
            if (currentX >= xMax) {
                currentX = 0.0
                currentY += ySize
            }
        }
    }

    fun refreshChart() {
        // refreshes charts
        chart.data.datasets = data.grainCountHistory.map {
            LineDataSet(
                it.key.description, it.value
                    .mapIndexed { index, i -> LineChartPlot(index, i) }
                    .toTypedArray(),
                borderColor = it.key.color, backgroundColor = it.key.color, fill = "false", showLine = false
            )
        }.toTypedArray()
        chart.update()
        lastRefresh = 0
    }
}

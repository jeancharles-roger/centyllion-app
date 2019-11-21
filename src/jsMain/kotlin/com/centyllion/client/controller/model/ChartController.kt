package com.centyllion.client.controller.model

import bulma.Div
import bulma.Label
import bulma.NoContextController
import bulma.SubTitle
import com.centyllion.client.controller.utils.push
import com.centyllion.client.page.BulmaPage
import uplot.Line
import kotlin.js.json
import kotlin.properties.Delegates.observable

data class ChartLine(
    val label: String, val color: String, val width: Int = 2, val initial: Number
)

data class Chart(
    val xLabel: String,
    val lines: List<ChartLine>
)

class ChartData(
    val xCount: Int,
    val data: Array<Array<Number>>
)

class ChartController(
    val page: BulmaPage, title: String, chart: Chart, size: Pair<Int, Int> = 800 to 400
): NoContextController<Chart, Div>() {

    private var chartData = ChartData(1, Array(chart.lines.size) { arrayOf(chart.lines[it].initial) })

    private var xValues = Array<Number>(chartData.xCount) { it }

    private var yValues: Array<Array<Number>> = chartData.data

    private val emptyChart = Label(page.i18n("No line to show"))

    private var uplot: Line? by observable(createUPlot(chart, size.first, size.second)) { _, old, new ->
        if (old != null) {
            container.root.removeChild(old.root)
        } else {
            container.root.removeChild(emptyChart.root)
        }
        if (new != null) {
            container.root.appendChild(new.root)
        } else {
            container.root.appendChild(emptyChart.root)
        }
    }

    var size: Pair<Int, Int> by observable(size) { _, old, new ->
        if (old != new) {
            // recreate uPlot
            uplot = createUPlot(data, new.first, new.second)
        }
    }

    override var data: Chart by observable(chart) { _, old, new ->
        if (old != new) {
            // resets chart data
            resetChartData()

            // recreate uPlot
            uplot = createUPlot(new, size.first, size.second)
        } else {
            // the chart is the same but the model was updated, a reset is required
            reset()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
    }

    val title = SubTitle(title).apply { root.classList.add("has-text-centered") }

    override val container: Div = Div(this.title).apply {
        if (uplot != null) {
            uplot?.let { root.appendChild(it.root) }
        } else {
            root.appendChild(emptyChart.root)
        }
    }

    override fun refresh() {
    }

    private fun createUPlot(chart: Chart, width: Int = 800, height: Int = 400): Line? {
        if (chart.lines.isEmpty()) return null
        val line = Line(
            json(
                "width" to width, "height" to height,
                "scales" to json(
                    "x" to json("time" to false),
                    "y" to json("time" to false, "range" to { _: Number, max: Number -> arrayOf(0, max.toDouble()*1.1) })
                ),
                "series" to json(
                    "x" to json("label" to data.xLabel),
                    "y" to chart.lines.map {
                        json(
                            "type" to false, "label" to it.label,
                            "color" to it.color, "width" to it.width
                        )
                    }.toTypedArray()
                )
            ),
            arrayOf(xValues, *yValues)
        )
        /** Toggle series that where toggled */
        val toggled = uplot?.series?.y?.filter{ !it.show }?.map { it.label } ?: emptyList()
        val toggledIds = line.series.y
            .mapIndexed { i, s -> i+1 to (!toggled.contains(s.label)) }
            .filter{ !it.second}
            .map { (i, _) -> i }
        if (toggledIds.isNotEmpty()) line.toggle(toggledIds.toTypedArray())

        return line
    }

    private fun resetChartData() {
        chartData = ChartData(1, Array(data.lines.size) { arrayOf(data.lines[it].initial) })
        xValues = Array(chartData.xCount) { it }
        yValues = chartData.data
    }

    fun reset() {
        resetChartData()
        uplot?.setData(arrayOf(xValues, *yValues), 0, xValues.lastOrNull() ?: 0)
    }

    fun push(x: Number, ys: Collection<Number>, refresh: Boolean) {
        xValues.push(x)
        ys.zip(yValues) { y, data -> data.push(y) }
        if (refresh) refreshData()
    }

    fun refreshData() {
        uplot?.setData(arrayOf(xValues, *yValues), 0, xValues.lastOrNull() ?: 0)
    }

}

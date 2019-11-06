package com.centyllion.client.controller.model

import bulma.Div
import bulma.Label
import bulma.NoContextController
import bulma.SubTitle
import com.centyllion.client.controller.utils.push
import com.centyllion.client.page.BulmaPage
import org.w3c.dom.HTMLElement
import kotlin.js.Json
import kotlin.js.json
import kotlin.math.max
import kotlin.properties.Delegates.observable

private external interface Serie {
    val label: String
    val type: String
    val scale: String
    val width: Number
    val show: Boolean
}

private external interface Series {
    val x: Serie
    val y: Array<Serie>
}

@JsModule("uPlot")
private external class uPlot(options: Json, data: Array<Array<Number>>) {

    val root: HTMLElement

    fun setData(data: Array<Array<out Number>>, minView: Number = definedExternally, maxView: Number = definedExternally)

    fun toggle(idxs: Array<Int>, onOff: Boolean)

    val series : Series
}


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

    private var xMax = xValues.map { n -> n.toDouble() }.max() ?: 0.0

    private var yMax = yValues.map { it.map { n -> n.toDouble() }.max() ?: 0.0 }

    private val emptyChart = Label(page.i18n("No line to show"))

    private var uplot: uPlot? by observable(createUPlot(chart, size.first, size.second)) { _, old, new ->
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

    private fun graphRangeX(min: Number, max: Number): Array<Number> {
        return arrayOf(min, max)
    }

    private fun graphRangeY(min: Number, max: Number): Array<Number> {
        val currentMax = yMax.filterIndexed { i, _ -> (uplot?.series?.y?.get(i)?.show) ?: false }.max() ?: 0.0
        return arrayOf(0, 1.1*currentMax)
    }

    private fun createUPlot(chart: Chart, width: Int = 800, height: Int = 400): uPlot? {
        if (chart.lines.isEmpty()) return null
        val toggled = uplot?.series?.y?.filter{ !it.show }?.map { it.label } ?: emptyList()
        return uPlot(
            json(
                "width" to width, "height" to height,
                "scales" to json(
                    "x" to json("type" to "n", "range" to ::graphRangeX),
                    "y" to json("type" to "n", "range" to ::graphRangeY)
                ),
                "series" to json(
                    "x" to json("label" to data.xLabel),
                    "y" to chart.lines.map {
                        json(
                            "type" to "n", "label" to it.label,
                            "show" to !toggled.contains(it.label),
                            "color" to it.color, "width" to it.width
                        )
                    }.toTypedArray()
                )
            ),
            arrayOf(xValues, *yValues)
        )
    }

    private fun resetChartData() {
        chartData = ChartData(1, Array(data.lines.size) { arrayOf(data.lines[it].initial) })
        xValues = Array(chartData.xCount) { it }
        yValues = chartData.data
        xMax = xValues.map { n -> n.toDouble() }.max() ?: 0.0
        yMax = yValues.map { it.map { n -> n.toDouble() }.max() ?: 0.0 }
    }

    fun reset() {
        resetChartData()
        uplot?.setData(arrayOf(xValues, *yValues), 0, xMax)
    }

    fun push(x: Number, ys: Collection<Number>) {
        xMax = maxOf(xMax, x.toDouble())
        xValues.push(x)
        yMax = yMax.zip(ys) { c, n -> max(c, n.toDouble()) }
        ys.zip(yValues) { y, data -> data.push(y) }
        uplot?.setData(arrayOf(xValues, *yValues), 0, xMax)
    }

}

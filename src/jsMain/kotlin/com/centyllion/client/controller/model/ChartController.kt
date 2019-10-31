package com.centyllion.client.controller.model

import bulma.Div
import bulma.NoContextController
import com.centyllion.client.controller.utils.push
import org.w3c.dom.HTMLElement
import kotlin.js.Json
import kotlin.js.json
import kotlin.properties.Delegates.observable


@JsName("uPlot")
private external class uPlot(options: Json, data: Array<Array<out Number>>) {

    val root: HTMLElement

    fun getView(): Array<Number>

    fun setData(data: Array<Array<out Number>>, minView: Number = definedExternally, maxView: Number = definedExternally)
}


data class ChartLine(
    val label: String, val color: String, val width: Int = 2
)

data class Chart(
    val xLabel: String,
    val lines: List<ChartLine>
)

class ChartData(
    val xCount: Int,
    val data: Array<Array<Int>>
)

class ChartController(
    chart: Chart, size: Pair<Int, Int> = 800 to 400
): NoContextController<Chart, Div>() {

    private var chartData = ChartData(1, Array(chart.lines.size) { emptyArray<Int>() })

    private var xValues = Array(chartData.xCount) { it }

    private var yValues: Array<Array<Int>> = chartData.data

    private var xMax = xValues.max() ?: 0

    private var yMax = yValues.map { it.max() ?: 0 }.max() ?: 0

    private var uplot: uPlot? by observable(createUPlot(chart, size.first, size.second)) { _, old, new ->
        if (old != null) container.root.removeChild(old.root)
        if (new != null) container.root.appendChild(new.root)
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
            chartData = ChartData(1, Array(new.lines.size) { emptyArray<Int>() })
            reset()

            // recreate uPlot
            uplot = createUPlot(new, size.first, size.second)
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
    }

    override val container: Div = Div().apply {
        uplot?.let { root.appendChild(it.root) }
    }

    override fun refresh() {
    }

    private fun graphRangeX(min: Number, max: Number): Array<Number> {
        return arrayOf(min, max)
    }

    private fun graphRangeY(min: Number, max: Number): Array<Number> {
        return arrayOf(0, yMax + (0.1 * yMax))
    }

    private fun createUPlot(chart: Chart, width: Int = 800, height: Int = 400): uPlot? {
        if (chart.lines.isEmpty()) return null
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
                        json("type" to "n", "label" to it.label, "color" to it.color, "width" to it.width)
                    }.toTypedArray()
                )
            ),
            arrayOf(xValues, *yValues)
        )
    }

    fun reset() {
        xValues = Array(chartData.xCount) { it }
        yValues = chartData.data
        xMax = xValues.max() ?: 0
        yMax = yValues.map { it.max() ?: 0 }.max() ?: 0
        uplot?.setData(arrayOf(xValues, *yValues), 0, xMax)
    }

    fun push(x: Int, ys: Collection<Int>) {
        xMax = maxOf(xMax, x)
        xValues.push(x)
        yMax = maxOf(yMax, ys.max() ?: 0)
        ys.zip(yValues) { y, data -> data.push(y) }
        uplot?.setData(arrayOf(xValues, *yValues), 0, xMax)
    }

}

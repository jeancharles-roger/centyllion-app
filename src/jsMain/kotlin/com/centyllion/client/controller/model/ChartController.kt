package com.centyllion.client.controller.model

import bulma.Div
import bulma.NoContextController
import com.centyllion.client.controller.utils.push
import uplot.AxeOption
import uplot.ScaleOptions
import uplot.SerieOptions
import uplot.UPlotOptions
import uplot.uPlot
import kotlin.js.json
import kotlin.properties.Delegates.observable


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
    chart: Chart
): NoContextController<Chart, Div>() {


    private var chartData = ChartData(1, Array(chart.lines.size) { emptyArray<Int>() })

    private var xValues = Array(chartData.xCount) { it }

    private var yValues: Array<Array<Int>> = chartData.data

    private var xMax = xValues.max() ?: 0

    private var yMax = yValues.map { it.max() ?: 0 }.max() ?: 0

    private var uplot: uPlot? by observable(createUPlot(chart)) { _, old, new ->
        if (old != null) container.root.removeChild(old.root)
        if (new != null) container.root.appendChild(new.root)
    }

    override var data: Chart by observable(chart) { _, old, new ->
        if (old != new) {
            // resets chart data
            chartData = ChartData(1, Array(new.lines.size) { emptyArray<Int>() })
            reset()

            // recreate uPlot
            uplot = createUPlot(new)
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
        return arrayOf(0, xMax)
    }

    private fun graphRangeY(min: Number, max: Number): Array<Number> {
        return arrayOf(0, yMax + (0.1 * yMax))
    }

    private fun createUPlot(chart: Chart): uPlot? {
        if (chart.lines.isEmpty()) return null
        return uPlot(
            UPlotOptions(800, 400).apply {
                scales = json(
                    "x" to ScaleOptions(type = "n", range = ::graphRangeX),
                    "y" to ScaleOptions(type = "n", range = ::graphRangeY)
                )
                axes.y = arrayOf(AxeOption(scale = "y"))
                series.x.label = data.xLabel
                series.y = chart.lines.map {
                    SerieOptions(
                        scale = "y", label = it.label, color = it.color,
                        width = it.width
                    )
                }.toTypedArray()
            },
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

package com.centyllion.client.controller.model

import bulma.Controller
import bulma.Div
import bulma.Label
import bulma.SubTitle
import com.centyllion.client.controller.utils.push
import com.centyllion.client.page.BulmaPage
import uplot.Size
import uplot.uPlot
import uplot.uPlot.Series
import kotlin.properties.Delegates.observable

data class ChartLine(
    val label: String, val initial: Number,
    val color: String, val fill: String? = null,
    val width: Int = 2, val dash: List<Int>? = null,
    val value: ((Number) -> String)? = null
) {
    internal fun options(show: Boolean) = object: Series {
        override var label: String? = this@ChartLine.label
        override var stroke: Any? = color
        override var fill: Any? = this@ChartLine.fill
        override var width: Any? = this@ChartLine.width
        override var dash: Array<Number>? = this@ChartLine.dash?.toTypedArray()
        override var values: ((self: uPlot, seriesIdx: Number, idx: Number) -> Any?)? =
            value?.let{ v -> { _: uPlot, raw: Number -> v(raw) } }
        override var show: Boolean? = show
    }
}

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
): Controller<Chart, Pair<Int, Int>, Div> {

    private var chartData = ChartData(1, Array(chart.lines.size) { arrayOf(chart.lines[it].initial) })

    private var xValues = Array<Number>(chartData.xCount) { it }

    private var yValues: Array<Array<Number>> = chartData.data

    private val emptyChart = Label(page.i18n("No line to show"))

    private var uplot: uPlot? by observable(createPlot(chart, size.first, size.second)) { _, old, new ->
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

    var size get() = context; set(value) { context = value }

    override var context: Pair<Int, Int> by observable(size) { _, old, new ->
        // re-sizes uPlot
        if (old != new) {
            uplot?.setSize(object: Size {
                override var width: Number = new.first
                override var height: Number = new.second
            })
        }
    }

    override var data: Chart by observable(chart) { _, old, new ->
        if (old != new) {
            // resets chart data
            resetChartData()

            // recreate uPlot
            uplot = createPlot(new, context.first, context.second)
        } else {
            // the chart is the same but the model was updated, a reset is required
            reset()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
    }

    val title = SubTitle(title).apply { root.classList.add("has-text-centered") }

    override val container: Div = Div(this.title).apply {
        root.appendChild(uplot?.root ?: emptyChart.root)
    }

    override fun refresh() {
    }

    private fun createPlot(chart: Chart, width: Int = 800, height: Int = 400): uPlot? {
        return null
        /*
        if (chart.lines.isEmpty()) return null
        //val toggled = uplot?.series?.filter { !it.show }?.map { it.label } ?: emptyList()
        val series = arrayOf<Series>(object : Series {
            override var label: String? = data.xLabel
            //override val time = false
        })
        //series += chart.lines.map { it.options(!toggled.contains(it.label)) }
        return uPlot(
            object: uPlot.Options {
                override var width: Number = width.coerceAtLeast(400)
                override var height: Number = height.coerceAtLeast(400)
                override var scales: Map<String, uPlot.Scale>? = mapOf("x" to
                    object : uPlot.Scale {
                        override var time: Boolean? = false

                })
                override var series: Array<Series> = series
            },
            arrayOf(xValues, *yValues)
        ) { uPlot: uPlot, function: Function<*> ->
             console.log("init")
        }
         */
    }

    private fun resetChartData() {
        chartData = ChartData(1, Array(data.lines.size) { arrayOf(data.lines[it].initial) })
        xValues = Array(chartData.xCount) { it }
        yValues = chartData.data
    }

    fun reset() {
        resetChartData()
        uplot?.setData(arrayOf(xValues, *yValues), true)
    }

    fun push(x: Number, ys: Collection<Number>, refresh: Boolean) {
        xValues.push(x)
        ys.zip(yValues) { y, data -> data.push(y) }
        if (refresh) refreshData()
    }

    fun refreshData() {
        uplot?.setData(arrayOf(xValues, *yValues), true)
    }

}

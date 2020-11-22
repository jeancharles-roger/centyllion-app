package com.centyllion.client.controller.model

import bulma.Controller
import bulma.Div
import bulma.Label
import bulma.SubTitle
import com.centyllion.client.controller.utils.push
import com.centyllion.client.page.BulmaPage
import uplot.Options
import uplot.Series
import uplot.Size
import uplot.uPlot
import kotlin.properties.Delegates.observable

data class ChartLine(
    val label: String, val initial: Number,
    val color: String, val fill: String? = null,
    val width: Int = 2, val dash: List<Int> = listOf(1),
    val value: ((Number) -> String)? = null
) {
    internal fun options(show: Boolean) = Series(
        auto = true,
        label = this@ChartLine.label,
        stroke = color,
        fill = this@ChartLine.fill,
        width = this@ChartLine.width,
        dash = this@ChartLine.dash.toTypedArray(),
        values = value?.let{ v -> { _: uPlot, raw: Number, idx: Number -> v(raw) } },
        show = show,
        scale = "y"
    )

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
            uplot?.setSize(Size(new.first,new.second))
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

    @OptIn(ExperimentalStdlibApi::class)
    private fun createPlot(chart: Chart, width: Int = 800, height: Int = 400): uPlot? {
        if (chart.lines.isEmpty()) return null
        //val toggled = uplot?.series?.filter { !it.show }?.map { it.label } ?: emptyList()

        val series = buildList {
            add(Series(label = data.xLabel, scale = "x"))
            addAll(chart.lines.map { it.options(true /*!toggled.contains(it.label)*/) })
        }
        return uPlot(
            Options(
                width = width.coerceAtLeast(400),
                height = height.coerceAtLeast(400),
                series = series.toTypedArray()
            ),
            arrayOf(xValues, *yValues)).apply {
                scales.asDynamic().x?.time = false
        }
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

package com.centyllion.client.controller.model

import bulma.Controller
import bulma.Div
import bulma.Label
import bulma.SubTitle
import com.centyllion.client.controller.utils.push
import com.centyllion.client.page.BulmaPage
import uplot.Size
import uplot.UPlot
import kotlin.js.json
import kotlin.properties.Delegates.observable

data class ChartLine(
    val label: String, val initial: Number,
    val color: String, val fill: String? = null,
    val width: Int = 2, val dash: List<Int>? = null,
    val value: ((Number) -> String)? = null
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
): Controller<Chart, Pair<Int, Int>, Div> {

    private var chartData = ChartData(1, Array(chart.lines.size) { arrayOf(chart.lines[it].initial) })

    private var xValues = Array<Number>(chartData.xCount) { it }

    private var yValues: Array<Array<Number>> = chartData.data

    private val emptyChart = Label(page.i18n("No line to show"))

    private var uplot: UPlot? by observable(createUPlot(chart, size.first, size.second)) { _, old, new ->
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
        console.log("Resize to $new")
        uplot?.setSize(object : Size {
            override val width = new.first.coerceAtLeast(801)
            override val height = new.second.coerceAtLeast(400)
        })
    }

    override var data: Chart by observable(chart) { _, old, new ->
        if (old != new) {
            // resets chart data
            resetChartData()

            // recreate uPlot
            uplot = createUPlot(new, context.first, context.second)
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

    private fun createUPlot(chart: Chart, width: Int = 800, height: Int = 400): UPlot? {
        if (chart.lines.isEmpty()) return null
        val toggled = uplot?.series?.filter{ !it.show }?.map { it.label } ?: emptyList()
        return UPlot(
            json(
                "width" to width.coerceAtLeast(800), "height" to height.coerceAtLeast(400),
                "scales" to json( "x" to json("time" to false)),
                //"cursor" to json("show" to false),
                //"legend" to json("show" to false),
                "series" to arrayOf(
                    json("label" to data.xLabel)
                ) + chart.lines.map {
                    val dataLine = json(
                        "show" to !toggled.contains(it.label),
                        "label" to it.label,
                        "stroke" to it.color,
                        "width" to it.width
                    )
                    if (it.fill != null) dataLine["fill"] = it.fill
                    if (it.dash != null) dataLine["dash"] = it.dash.toTypedArray()
                    if (it.value != null) dataLine["value"] = { _: UPlot, raw: Number -> it.value.invoke(raw)}
                    dataLine
                }.toTypedArray()
            ),
            arrayOf(xValues, *yValues)
        )
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

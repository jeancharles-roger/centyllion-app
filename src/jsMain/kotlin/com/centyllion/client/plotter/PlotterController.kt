package com.centyllion.client.plotter

import bulma.Column
import bulma.Columns
import bulma.Controller
import bulma.Div
import bulma.HtmlWrapper
import bulma.Icon
import bulma.Level
import bulma.SubTitle
import bulma.canvas
import bulma.span
import com.centyllion.client.page.BulmaPage
import io.data2viz.geom.Size
import io.data2viz.geom.size
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable

class PlotterController(
    val page: BulmaPage, title: String,
    plot: List<Plot>, size: Size = size(800.0, 400.0)
): Controller<List<Plot>, Size, Div> {

    val canvas = canvas { }

    override var data: List<Plot> by observable(plot) { _, old, new ->
        if (old != new) {
            // recreate uPlot
            plotter = createPlotter(canvas.root, new, context)
            legend.columns = createColumns(new)
        } else {
            // the chart is the same but the model was updated, a reset is required
            reset()
        }
    }

    private var plotter: LinePlotter = createPlotter(canvas.root, data, size)

    var size get() = context; set(value) { context = value }

    override var context: Size by observable(size) { _, old, new ->
        if (old != new) {
            plotter.size = new
            canvas.root.width = context.width.toInt()
            canvas.root.height = context.height.toInt()
        }
    }

    override var readOnly: Boolean = false

    val title = SubTitle(title).apply { root.classList.add("has-text-centered") }

    private val legendValueSpans = mutableListOf<HtmlWrapper<HTMLElement>>()

    val legend = Columns(multiline = true).apply { columns = createColumns(data) }

    override val container: Div = Div(this.title, canvas, legend)

    override fun refresh() { }

    private fun createPlotter(canvas: HTMLCanvasElement, plots: List<Plot>, size: Size): LinePlotter {
        return LinePlotter(canvas, plots, size) { step ->
            legendValueSpans.forEachIndexed { index, valueSpan ->
                valueSpan.text =
                    if (step >= 0 && step < plotter.labels.size) "${plotter.plotPoints[index][step]}"
                    else "-"
            }
        }
    }

    private fun colorIcon(color: String) = Icon("").apply {
        root.style.backgroundColor = color
        root.style.borderRadius = "50%"
        root.style.color = color
        root.style.marginRight = "0.5rem"
    }

    private fun createColumns(plots: List<Plot>): List<Column> {
        legendValueSpans.clear()
        return plots.map {
            val valueSpan = span("0")
            legendValueSpans.add(valueSpan)
            Column(
                Level(
                    center = listOf(
                        colorIcon(it.stroke.rgbHex), span(it.label),
                        span(":"), valueSpan
                    )
                ),
                narrow = true
            )
        }
    }

    fun reset() {
        plotter.clear()
    }

    fun push(x: Double, ys: List<Double>) {
        plotter.push(x, ys)
    }

    fun renderRequest() {
        plotter.renderRequest()
    }
}

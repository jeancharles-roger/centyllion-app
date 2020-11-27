package com.centyllion.client.controller.model

import bulma.Controller
import bulma.Div
import bulma.SubTitle
import bulma.canvas
import com.centyllion.client.page.BulmaPage
import com.centyllion.client.plotter.LinePlotter
import com.centyllion.client.plotter.Plot
import io.data2viz.geom.Size
import io.data2viz.geom.size
import org.w3c.dom.HTMLCanvasElement
import kotlin.properties.Delegates.observable

class ChartController(
    val page: BulmaPage, title: String,
    plot: List<Plot>, size: Size = size(800.0, 400.0)
): Controller<List<Plot>, Size, Div> {

    val canvas = canvas { }

    override var data: List<Plot> by observable(plot) { _, old, new ->
        if (old != new) {
            // recreate uPlot
            plotter = createPlotter(canvas.root, new, context)
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

    override var readOnly: Boolean by observable(false) { _, old, new ->
    }

    val title = SubTitle(title).apply { root.classList.add("has-text-centered") }

    override val container: Div = Div(this.title, canvas)

    override fun refresh() {
    }

    private fun createPlotter(canvas: HTMLCanvasElement, plots: List<Plot>, size: Size): LinePlotter {
        //val toggled = uplot?.series?.filter { !it.show }?.map { it.label } ?: emptyList()
        return LinePlotter(canvas, plots, size)
    }

    fun reset() {
        plotter.clear()
    }

    fun push(x: Double, ys: List<Double>) {
        plotter.push(x, ys)
        //if (refresh) refreshData()
    }

    fun renderRequest() {
        plotter.renderRequest()
    }
}

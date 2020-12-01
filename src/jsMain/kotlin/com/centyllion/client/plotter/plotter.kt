package com.centyllion.client.plotter

import com.centyllion.model.colorNames
import io.data2viz.axis.Orient
import io.data2viz.axis.axis
import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.geom.Size
import io.data2viz.math.pct
import io.data2viz.scale.Scales
import io.data2viz.viz.*
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.roundToInt
import kotlin.properties.Delegates.observable

data class Range(val min: Double, val max: Double)

fun range(min: Double, max: Double) = Range(min, max)

fun String.toRGB(): Color = colorNames[this]?.let { Colors.rgb(it.first, it.second, it.third, 100.pct) } ?: Colors.Web.red

data class Plot(
    val label: String,
    val stroke: Color = Colors.Web.steelblue,
    val strokeWidth: Double = 1.0,
)

private data class PlotPoint(
    val step: Int, val points: List<Double>
)

class LinePlotter(
    val canvas: HTMLCanvasElement, val plots: List<Plot>, size: Size,
    val onStepMove: (Int) -> Unit = { }
) {
    var size: Size by observable(size) { _, old, new ->
        if (old != new) {
            visual.size = new
            xScale = newXScale()
            yScale = newYScale()
            rebuild()
        }
    }

    private val points: MutableList<PlotPoint> = mutableListOf()

    // TODO make margins and size mutable
    // TODO checks that margins aren't bigger than size
    val margins = Margins(40.5, 30.5, 50.5, 50.5)

    val chartWidth get() = size.width - margins.hMargins
    val chartHeight get() = size.height - margins.vMargins

    var xTick: Int = 1

    var xMax: Int by observable(0) { _, old, new ->
        if (old != new) {
            xScale = newXScale()
            rebuild()
        }
    }

    var yTick: Double = 1.0

    var yRange: Range by observable(computeYRange()) { _, old, new ->
        if (old != new) {
            yScale = newYScale()
            rebuild()
        }
    }

    private fun computeYRange() = range(
        points.flatMap { it.points.filterIndexed { index, d -> !hiddenPlots[index] } }.minOrNull() ?: 0.0,
        points.flatMap { it.points.filterIndexed { index, d -> !hiddenPlots[index] } }.maxOrNull() ?: 0.0,
    )

    // linear scale for x
    private var xScale = newXScale()

    private fun newXScale() = Scales.Continuous.linearRound {
        domain = listOf(0.0, xMax.toDouble())
        range = listOf(0.0, chartWidth)
    }

    // linear scale for y
    private var yScale = newYScale()

    private fun newYScale() = Scales.Continuous.linear {
        domain = listOf(yRange.min, yRange.max)
        range = listOf(chartHeight, 0.0) // <- y is mapped in the reverse order (in SVG, javafx (0,0) is top left.
    }

    private fun GroupNode.xAxis() = group {
        transform { translate(y = chartHeight + 10) }
        axis(Orient.BOTTOM, xScale)
    }

    private fun GroupNode.yAxis() = group {
        transform { translate(x = -10.0) }
        axis(Orient.LEFT, yScale)
    }

    private val hiddenPlots = Array(plots.size) { false }

    private var plotPaths = mutableListOf<PathNode>()

    private var redraw = false

    private fun Viz.build() {
        clear()
        group {
            transform { translate(x = margins.left, y = margins.top) }
            group { yAxis() }
            group { xAxis() }

            group {
                plotPaths.clear()
                if (points.isNotEmpty()) {
                    for (plotIndex in plots.indices) {
                        group {
                            plotPaths.add(path {
                                fill = null
                                val element = plots[plotIndex]
                                stroke = if (!hiddenPlots[plotIndex]) element.stroke else null
                                strokeWidth = element.strokeWidth

                                moveTo(xScale(points[0].step), yScale(points[0].points[plotIndex]))
                                for (i in 1 until points.size) {
                                    lineTo(xScale(points[i].step), yScale(points[i].points[plotIndex]))
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    private var currentStep = -1

    val visual: Viz = viz {
        this.size = this@LinePlotter.size
        build()

        on(KMouseMove) {
            // TODO there is a DPI problem with the position, it needs to be solved
            val step = xScale.invert(it.pos.x-margins.left).roundToInt().coerceIn(0, xMax)
            if (step != currentStep) onStepMove(step)
            currentStep = step
        }

        bindRendererOn(canvas)
    }

    private fun findNewXMax(current: Int, pushed: Int, tick: Int): Int {
        var newMax = current
        while (newMax < pushed) newMax += tick
        return newMax
    }

    private fun findNewMax(current: Double, pushed: Double, tick: Double): Double {
        var newMax = current
        while (newMax < pushed) newMax += tick
        return newMax
    }

    private fun findNewMin(current: Double, pushed: Double, tick: Double): Double {
        var newMin = current
        while (newMin > pushed) newMin -= tick
        return newMin
    }

    fun push(x: Int, ys: List<Double>) {
        // register point
        points.add(PlotPoint(x, ys))

        var newMin = yRange.min
        var newMax = yRange.max
        for (i in plots.indices) {
            // updates min and max if not hidden
            if (!hiddenPlots[i]) {
                if (ys[i] < newMin) newMin = ys[i]
                if (ys[i] > newMax) newMax = ys[i]
            }
        }

        // updates ranges
        xMax = findNewXMax(xMax, x, xTick)
        yRange = range(findNewMin(yRange.min, newMin, yTick), findNewMax(yRange.max, newMax, yTick))

        // update paths
        for (i in plotPaths.indices) {
            if (!hiddenPlots[i]) plotPaths[i].lineTo(xScale(x), yScale(ys[i]))
        }
        invalidate()
    }

    fun invalidate() {
        redraw = true
    }

    fun renderRequest() {
        if (redraw) {
            visual.render()
            redraw = false
        }
    }

    fun rebuild() {
        visual.build()
        invalidate()
    }

    fun clear() {
        points.clear()
        xMax = 0
        yRange = range(0.0, 0.0)
        rebuild()
    }

    fun pointsForLabel(step: Int) = points.find { it.step == step }?.points

    fun isHidden(plot: Plot) = plots.indexOf(plot).let {
        if (it >= 0) hiddenPlots[it] else false
    }
    
    fun toggleHiddenPlot(plot: Plot): Boolean {
        val index = plots.indexOf(plot)
        return if (index >= 0) {
            val new = !hiddenPlots[index]
            hiddenPlots[index] = new
            yRange = computeYRange()
            rebuild()
            renderRequest()
            new
        } else false
    }
}


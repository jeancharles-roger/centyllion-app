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
import kotlin.math.max
import kotlin.properties.Delegates.observable

data class Range(val min: Double, val max: Double)

fun range(min: Double, max: Double) = Range(min, max)

fun String.toRGB(): Color = colorNames[this]?.let { Colors.rgb(it.first, it.second, it.third, 100.pct) } ?: Colors.Web.red

class Plot(
    val label: String,
    val stroke: Color = Colors.Web.steelblue,
    val strokeWidth: Double = 1.0,
)

class LinePlotter(val canvas: HTMLCanvasElement, val plots: List<Plot>, size: Size) {

    var size: Size by observable(size) { _, old, new -> if (old != new) visual.size = new }

    val labels: MutableList<Double> = mutableListOf()

    val plotPoints: List<MutableList<Double>> = List(plots.size) { mutableListOf() }

    // TODO make margins and size mutable
    // TODO checks that margins aren't bigger than size
    val margins = Margins(40.5, 30.5, 50.5, 50.5)

    val chartWidth get() = size.width - margins.hMargins
    val chartHeight get() = size.height - margins.vMargins

    var xTick: Double = 1.0

    var xRange: Range by observable(
        range(labels.firstOrNull() ?: 0.0, labels.lastOrNull() ?: 0.0)
    ) { _, old, new ->
        if (old != new) {
            xScale = newXScale()
            rebuild()
        }
    }

    var yTick: Double = 1.0

    var yRange: Range by observable(
        range(
            plotPoints.mapNotNull { it.minOrNull() }.minOrNull() ?: 0.0,
            plotPoints.mapNotNull { it.maxOrNull() }.maxOrNull() ?: 0.0,
        )
    ) { _, old, new ->
        if (old != new) {
            yScale = newYScale()
            rebuild()
        }
    }

    // linear scale for x
    private var xScale = newXScale()

    private fun newXScale() = Scales.Continuous.linear {
        domain = listOf(xRange.min, xRange.max)
        range = listOf(.0, chartWidth)
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
                if (labels.isNotEmpty()) {
                    for (plotIndex in plots.indices) {
                        group {
                            plotPaths.add(path {
                                fill = null
                                stroke = plots[plotIndex].stroke
                                strokeWidth = plots[plotIndex].strokeWidth

                                moveTo(xScale(labels[0]), yScale(plotPoints[plotIndex][0]))
                                for (i in 1 until labels.size) {
                                    lineTo(xScale(labels[i]), yScale(plotPoints[plotIndex][i]))
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    val visual: Viz = viz {
        this.size = this@LinePlotter.size
        build()
    }.apply { bindRendererOn(canvas) }

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

    fun push(x: Double, ys: List<Double>) {
        labels.add(x)

        var newMin = yRange.min
        var newMax = yRange.max
        for (i in plots.indices) {
            val y = ys[i]
            // registers point
            plotPoints[i].add(y)

            // updates min and max
            if (y < newMin) newMin = y
            if (y > newMax) newMax = y
        }

        // updates ranges
        xRange = range(xRange.min, max(xRange.max, findNewMax(xRange.max, x, xTick)))
        yRange = range(findNewMin(yRange.min, newMin, yTick), findNewMax(yRange.max, newMax, yTick))

        // update paths
        for (i in plots.indices) {
            plotPaths[i].lineTo(xScale(x), yScale(ys[i]))
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
        labels.clear()
        plotPoints.forEach { it.clear() }
        xRange = range(0.0, 0.0)
        yRange = range(0.0, 0.0)
        rebuild()
    }
}


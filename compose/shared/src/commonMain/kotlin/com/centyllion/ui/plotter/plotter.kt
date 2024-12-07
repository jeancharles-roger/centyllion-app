package com.centyllion.ui.plotter

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.roundToInt

class Plotter {

    class PlotLine(
        val color: Color,
        val size: Int,
        val valueAt: (Int) -> Float
    )

    val stepsCache = mutableMapOf<Int, TextLine>()

    val font = Font().also { it.size = 20f }

    val leftMargin: Float = 20f
    val bottomMargin: Float = 20f

    fun ticksFor(max: Int): List<Int> {
        val floatSteps = max.coerceAtLeast(1).toFloat()
        val digits = floor(log(floatSteps, 10f)).roundToInt()
        val ticks = (floatSteps / 10f.pow(digits)).roundToInt()
        return List(ticks + 1) { it * 10f.pow(digits).roundToInt() }
    }

    fun plot(scope: DrawScope, maxStep: Int, maxY: Number, lines: List<PlotLine>) = with(scope) {
        clipRect {
            val maxYfloat = maxY.toFloat()
            val innerWidth = leftMargin
            val innerHeight = size.height - bottomMargin
            val xStep = size.width / maxStep.coerceAtLeast(1)
            val yStep = size.height / maxYfloat.roundToInt().coerceAtLeast(1)

            // draw step scale
            val paint = Paint()
            val maxStepLine = TextLine.make(maxStep.toString(), font)
            drawIntoCanvas {
                it.nativeCanvas.drawTextLine(
                    maxStepLine,
                    size.width - maxStepLine.width,
                    size.height,
                    paint
                )
            }

            // draws value scale
            drawLine(Color.Black, Offset(innerWidth, 0f), Offset(innerWidth, innerHeight), 2f)
            ticksFor(maxYfloat.roundToInt() + 1).forEach { tick ->
                val y = innerHeight - (yStep * tick)
                drawLine(Color.Black, Offset(innerWidth, y), Offset(0f, y), 2f)

                val textLine = stepsCache.getOrPut(tick) { TextLine.make("$tick", font) }
                drawIntoCanvas {
                    it.nativeCanvas.drawTextLine(textLine, 0f, y - 5f, paint)
                }
            }

            // draws time scale
            drawLine(Color.Black, Offset(innerWidth, innerHeight), Offset(size.width, innerHeight), 2f)
            ticksFor(maxStep).forEach { tick ->
                val x = innerWidth + xStep * tick
                drawLine(Color.Black, Offset(x, innerHeight), Offset(x, size.height), 2f)

                val textLine = stepsCache.getOrPut(tick) { TextLine.make("$tick", font) }
                drawIntoCanvas {
                    it.nativeCanvas.drawTextLine(textLine, x + 5f, size.height, paint)
                }
            }

            // draws line
            if (maxYfloat > 0f) {
                val yStep = innerHeight / maxYfloat
                lines.forEach { line ->
                    val path = Path()
                    repeat(line.size) { step ->
                        val valueFloat = line.valueAt(step)
                        if (step == 0) path.moveTo(innerWidth, innerHeight - valueFloat * yStep)
                        else path.lineTo(innerWidth + (step * xStep), innerHeight - valueFloat * yStep)
                    }
                    drawPath(path, line.color, style = Stroke(2f))
                }
            }
        }
    }

}
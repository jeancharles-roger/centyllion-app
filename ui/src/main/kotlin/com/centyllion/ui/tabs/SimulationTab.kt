package com.centyllion.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.centyllion.model.colorNames
import com.centyllion.model.minFieldLevel
import com.centyllion.ui.AppContext
import com.centyllion.ui.allIcons
import com.centyllion.ui.alphaColor
import com.centyllion.ui.color
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.SquareFull
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.roundToInt

object SimulationTab : Tab {

    override val nameKey = "Simulation"
    override val icon = FontAwesomeIcons.Solid.Play

    class GrainInfo(
        val color: Color,
        val painter: VectorPainter?,
        val tint: ColorFilter = ColorFilter.tint(color)
    )

    val defaultInfo = GrainInfo(Color.Red, null)

    val font = Font().also { it.size = 20f }


    @Composable
    override fun content(appContext: AppContext) {
        val model = appContext.model
        val grainInfos = model.grains.associate {
            val color = colorNames[it.color]?.color ?: Color.Red
            val icon = allIcons[it.iconName] ?: FontAwesomeIcons.Solid.SquareFull
            it.id to GrainInfo(color, rememberVectorPainter(icon))
        }

        Canvas(Modifier.fillMaxSize()) {
            val simulator = appContext.simulator
            val simulation = appContext.simulation

            val step = min(size.width/simulation.width, size.height/simulation.height)
            val sixthStep = step/6

            // draws background TODO configurable background color or image
            drawRect(Color.Gray)

            var x = 0
            for (i in 0 until simulation.dataSize) {
                // draw fields
                for (pair in appContext.simulator.fields) {
                    val field = model.fieldForId(pair.key)
                    if (field != null && !field.invisible) {
                        val fieldValue = simulator.field(field.id)[i]
                        val alpha = fieldValue.alpha
                        if (alpha > 0f) {
                            // TODO search for field color only once
                            val color = colorNames[field.color]?.alphaColor(alpha) ?: Color.Red
                            drawRect(color, Offset.Zero, Size(step, step))
                        }
                    }
                }

                // draw grains
                val id = simulator.idAtIndex(i)
                val grain = appContext.model.grainForId(id)
                if (grain != null && !grain.invisible) {
                    val info = grainInfos[grain.id] ?: defaultInfo

                    // TODO search for grain color only once
                    when (grain.iconName) {
                        "square" -> drawRect(info.color, Offset(sixthStep, sixthStep), Size(4*sixthStep, 4*sixthStep))
                        "squarefull" -> drawRect(info.color, Offset.Zero, Size(step, step))
                        "circle" -> drawCircle(info.color, step/2f, Offset(3*sixthStep, 3*sixthStep))
                        else -> {
                            info.painter?.apply { draw(Size(step, step), colorFilter = info.tint) }
                                ?: drawRect(info.color, Offset.Zero, Size(step, step))
                        }
                    }
                }

                // updates coordinates
                if (x >= simulation.width-1) {
                    x = 0
                    drawContext.transform.translate(-step*(simulation.width-1), step)
                } else {
                    x += 1
                    drawContext.transform.translate(step, 0f)
                }
            }

            val line = TextLine.make(appContext.step.toString(), font)
            drawIntoCanvas { it.nativeCanvas.drawTextLine(line, 0f, 0f, Paint()) }
        }
    }
}

val Float.alpha get() = when {
    this <= minFieldLevel -> 0f
    this >= 1f -> 255f
    this >= 0.1f -> 150 * this + 89f
    else -> (-100f / log10(this))
}.roundToInt()
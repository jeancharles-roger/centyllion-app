package com.centyllion.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.centyllion.model.colorNames
import com.centyllion.model.minFieldLevel
import com.centyllion.ui.AppContext
import com.centyllion.ui.alphaColor
import com.centyllion.ui.color
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.roundToInt

object SimulationTab : Tab {

    override val nameKey = "Simulation"
    override val icon = FontAwesomeIcons.Solid.Play

    val font = Font().also { it.size = 20f }

    @Composable
    override fun content(appContext: AppContext) {
        Canvas(Modifier.fillMaxSize()) {
            val simulator = appContext.simulator
            val model = appContext.model
            val simulation = appContext.simulation

            val step = min(size.width/simulation.width, size.height/simulation.height)
            val sixthStep = step/6

            // draws background
            drawRect(Color.Gray)

            var x = 0
            var y = 0
            for (i in 0 until simulation.dataSize) {
                // draw fields
                for (pair in appContext.simulator.fields) {
                    val field = model.fieldForId(pair.key)
                    if (field != null && !field.invisible) {
                        val fieldValue = simulator.fieldAt(field.id, i)
                        val alpha = fieldValue.alpha
                        if (alpha > 0f) {
                            // TODO search for field color only once
                            val color = colorNames[field.color]?.alphaColor(alpha) ?: Color.Red
                            drawRect(color, Offset(x * step, y * step), Size(step, step))
                        }
                    }
                }

                // draw grains
                val id = simulator.idAtIndex(i)
                val grain = appContext.model.grainForId(id)
                if (grain != null) {
                    // TODO search for grain color only once
                    val color = colorNames[grain.color]?.color ?: Color.Red
                    when (grain.iconName) {
                        "square" -> drawRect(color, Offset(x*step + sixthStep, y*step + sixthStep), Size(4*sixthStep, 4*sixthStep))
                        "squarefull" -> drawRect(color, Offset(x*step, y*step), Size(step, step))
                        "circle" -> drawCircle(color, step/2f, Offset(x*step + 3*sixthStep, y*step + 3*sixthStep))
                        else -> drawRect(color, Offset(x*step, y*step), Size(step, step))
                    }
                }

                // updates coordinates
                if (x >= simulation.width-1) {
                    x = 0
                    y += 1
                } else {
                    x += 1
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
package com.centyllion.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.centyllion.model.colorNames
import com.centyllion.ui.AppContext
import com.centyllion.ui.color
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChartLine
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine

object FieldsPlotterTab : Tab {

    override val nameKey = "Fields Plotter"
    override val icon = FontAwesomeIcons.Solid.ChartLine

    @Composable
    override fun content(appContext: AppContext) {
        val simulator = appContext.simulator

        val visibleFields = remember { mutableStateOf(
            appContext.model.fields.map { it.id }.toSet()
        ) }

        Canvas(Modifier.fillMaxSize()) {
            val height = size.height
            if (appContext.step > 0) {
                val xStep = size.width / appContext.step
                val maxFieldAmount = simulator.maxFieldAmount
                    .filterKeys { visibleFields.value.contains(it) }
                    .values.maxOrNull() ?: 0f

                if (maxFieldAmount > 0f) {
                    val yStep = height / maxFieldAmount

                    simulator.fieldAmountHistory
                        .filter { visibleFields.value.contains(it.key.id) }
                        .forEach { (field, values) ->
                            val path = Path()

                            // synchronized to avoid concurrent modification
                            synchronized(simulator) {
                                values.forEachIndexed { step, value ->
                                    if (step == 0) path.moveTo(step * xStep, height - value * yStep)
                                    else path.lineTo(step * xStep, height - value * yStep)
                                }
                            }

                            val color = colorNames[field.color]?.color ?: Color.Red
                            drawPath(path, color, style = Stroke(2f))
                        }
                }

                val line = TextLine.make(appContext.step.toString(), SimulationTab.font)
                drawIntoCanvas { it.nativeCanvas.drawTextLine(line, 0f, height, Paint()) }
            }
        }

        Text(appContext.locale.i18n("Grains"))
    }
}

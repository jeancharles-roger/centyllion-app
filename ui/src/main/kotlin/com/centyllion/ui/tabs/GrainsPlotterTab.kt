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

object GrainsPlotterTab : Tab {

    override val nameKey = "Grains Plotter"
    override val icon = FontAwesomeIcons.Solid.ChartLine

    @Composable
    override fun content(appContext: AppContext) {
        val simulator = appContext.simulator

        val visibleGrains = remember { mutableStateOf(
            appContext.model.grains
                .filter { appContext.model.doesGrainCountCanChange(it) }
                .map { it.id }
                .toSet()
        ) }

        Canvas(Modifier.fillMaxSize()) {
            val height = size.height
            if (appContext.step > 0) {
                val xStep = size.width / appContext.step
                val maxGrainCount = simulator.maxGrainCount
                    .filterKeys { visibleGrains.value.contains(it) }
                    .values.maxOrNull() ?: 0

                if (maxGrainCount > 0) {
                    val yStep = height / maxGrainCount

                    simulator.grainCountHistory
                        .filter { visibleGrains.value.contains(it.key.id) }
                        .forEach { (grain, values) ->
                            val path = Path()

                            // synchronized to avoid concurrent modification
                            synchronized(simulator) {
                                values.forEachIndexed { step, value ->
                                    if (step == 0) path.moveTo(step * xStep, height - value * yStep)
                                    else path.lineTo(step * xStep, height - value * yStep)
                                }
                            }

                            val color = colorNames[grain.color]?.color ?: Color.Red
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

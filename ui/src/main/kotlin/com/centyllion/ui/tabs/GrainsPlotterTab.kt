package com.centyllion.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.centyllion.model.colorNames
import com.centyllion.ui.AppContext
import com.centyllion.ui.color
import com.centyllion.ui.plotter.Plotter
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChartLine

object GrainsPlotterTab : Tab {

    override val nameKey = "Grains Plotter"
    override val icon = FontAwesomeIcons.Solid.ChartLine

    val plotter = Plotter()

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
            val maxGrainCount = simulator.maxGrainCount
                .filterKeys { visibleGrains.value.contains(it) }
                .values.maxOrNull() ?: 0

            val lines = simulator.grainCountHistory
                .filter { visibleGrains.value.contains(it.key.id) }
                .map { (grain, values) ->
                    val color = colorNames[grain.color]?.color ?: Color.Red
                    Plotter.PlotLine(color, values.size) { values[it].toFloat() }
                }
            plotter.plot(this, appContext.step, maxGrainCount, lines)
        }
        Text(appContext.locale.i18n("Grains"))
    }

}

package com.centyllion.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.centyllion.model.Simulator
import com.centyllion.model.colorNames
import com.centyllion.ui.AppContext
import com.centyllion.ui.color
import com.centyllion.ui.plotter.Plotter
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChartLine

object PlotterTab : Tab {

    override val nameKey = "Plotters"
    override val icon = FontAwesomeIcons.Solid.ChartLine

    @Composable
    override fun content(app: AppContext) {
        Column(Modifier.fillMaxSize()) {
            grainPlot(app, Modifier.weight(1f).fillMaxWidth(), app.simulator)
            fieldPlot(app, Modifier.weight(1f).fillMaxWidth(), app.simulator)
        }
    }

    @Composable
    private fun grainPlot(appContext: AppContext, modifier: Modifier, simulator: Simulator) {
        val grainPlotter = remember { Plotter() }

        val visibleGrains = remember {
            mutableStateOf(
                appContext.modelAndSimulation.model.grains
                    .filter { appContext.modelAndSimulation.model.doesGrainCountCanChange(it) }
                    .map { it.id }
                    .toSet()
            )
        }

        Canvas(modifier) {
            val maxGrainCount = simulator.maxGrainCount
                .filterKeys { visibleGrains.value.contains(it) }
                .values.maxOrNull() ?: 0

            val lines = simulator.grainCountHistory
                .filter { visibleGrains.value.contains(it.key.id) }
                .map { (grain, values) ->
                    val color = colorNames[grain.color]?.color ?: Color.Red
                    Plotter.PlotLine(color, values.size) { values[it].toFloat() }
                }
            grainPlotter.plot(this, appContext.step, maxGrainCount, lines)
        }
    }

    @Composable
    private fun fieldPlot(appContext: AppContext, modifier: Modifier, simulator: Simulator) {
        val fieldPlotter = remember { Plotter() }

        val visibleFields = remember {
            mutableStateOf(
                appContext.modelAndSimulation.model.fields.map { it.id }.toSet()
            )
        }

        Canvas(modifier) {
            val maxFieldAmount = simulator.maxFieldAmount
                .filterKeys { visibleFields.value.contains(it) }
                .values.maxOrNull() ?: 0f

            val lines = simulator.fieldAmountHistory
                .filter { visibleFields.value.contains(it.key.id) }
                .map { (field, values) ->
                    val color = colorNames[field.color]?.color ?: Color.Red
                    Plotter.PlotLine(color, values.size) { values[it] }
                }
            fieldPlotter.plot(this, appContext.step, maxFieldAmount, lines)
        }
    }
}
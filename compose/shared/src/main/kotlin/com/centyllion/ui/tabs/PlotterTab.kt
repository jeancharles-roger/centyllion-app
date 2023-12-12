package com.centyllion.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.centyllion.model.Simulator
import com.centyllion.model.colorNames
import com.centyllion.ui.AppContext
import com.centyllion.ui.color
import com.centyllion.ui.plotter.Plotter
import com.centyllion.ui.verticalSplitter
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChartLine
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

object PlotterTab: Tab {

    override val nameKey = "Plotters"
    override val icon = FontAwesomeIcons.Solid.ChartLine


    private val grainPlotter = Plotter()
    private val fieldPlotter = Plotter()

    @Composable @OptIn(ExperimentalSplitPaneApi::class)
    override fun content(appContext: AppContext) {
        val simulator = appContext.simulator
        VerticalSplitPane(
            splitPaneState = rememberSplitPaneState(.5f),
            modifier = Modifier.background(appContext.theme.colors.background).padding(bottom = 20.dp)
        ) {
            first { grainPlot(appContext, simulator) }
            second { fieldPlot(appContext, simulator) }
            verticalSplitter()
        }
    }

    @Composable
    private fun grainPlot(appContext: AppContext, simulator: Simulator) {
        val visibleGrains = remember {
            mutableStateOf(
                appContext.modelAndSimulation.model.grains
                    .filter { appContext.modelAndSimulation.model.doesGrainCountCanChange(it) }
                    .map { it.id }
                    .toSet()
            )
        }

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
            grainPlotter.plot(this, appContext.step, maxGrainCount, lines)
        }
    }

    @Composable
    private fun fieldPlot(appContext: AppContext, simulator: Simulator) {
        val visibleFields = remember {
            mutableStateOf(
                appContext.modelAndSimulation.model.fields.map { it.id }.toSet()
            )
        }

        Canvas(Modifier.fillMaxSize()) {
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
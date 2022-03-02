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

object FieldsPlotterTab : Tab {

    override val nameKey = "Fields Plotter"
    override val icon = FontAwesomeIcons.Solid.ChartLine

    val plotter = Plotter()

    @Composable
    override fun content(appContext: AppContext) {
        val simulator = appContext.simulator

        val visibleFields = remember { mutableStateOf(
            appContext.model.fields.map { it.id }.toSet()
        ) }

        Canvas(Modifier.fillMaxSize()) {
            if (appContext.step > 0) {
                val maxFieldAmount = simulator.maxFieldAmount
                    .filterKeys { visibleFields.value.contains(it) }
                    .values.maxOrNull() ?: 0f

                if (maxFieldAmount > 0f) {
                    val lines = simulator.fieldAmountHistory
                        .filter { visibleFields.value.contains(it.key.id) }
                        .map { (field, values) ->
                            val color = colorNames[field.color]?.color ?: Color.Red
                            Plotter.PlotLine(color, values.toList())
                        }
                    plotter.plot(this, appContext.step, maxFieldAmount, lines)
                }
            }
        }

        Text(appContext.locale.i18n("Grains"))
    }
}

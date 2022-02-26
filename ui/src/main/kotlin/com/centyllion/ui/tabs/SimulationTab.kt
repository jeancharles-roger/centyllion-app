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
import com.centyllion.ui.AppContext
import com.centyllion.ui.color
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Play
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import kotlin.math.min

object SimulationTab : Tab {

    override val nameKey = "Simulation"
    override val icon = FontAwesomeIcons.Solid.Play

    val font = Font().also { it.size = 20f }

    @Composable
    override fun content(appContext: AppContext) {
        Canvas(Modifier.fillMaxSize()) {
            val simulator = appContext.simulator
            val simulation = appContext.simulation
            var x = 0
            var y = 0
            val step = min(size.width/simulation.width, size.height/simulation.height)
            val agents = simulator.currentAgents
            for (i in 0 until simulation.dataSize) {
            //for (agentId in agents) {
                val id = simulator.idAtIndex(i)
                val grain = appContext.model.grainForId(id)
                if (grain != null) {
                    val color = colorNames[grain.color]?.color ?: Color.Red
                    drawRect(color, Offset(x*step, y*step), Size(step, step))
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
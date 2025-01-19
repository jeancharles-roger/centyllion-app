package com.centyllion.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centyllion.model.Grain
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ToolBar(app: AppState) {

    val scope = rememberCoroutineScope()

    TopAppBar(
        backgroundColor = AppTheme.colors.background,
        modifier = Modifier.height(40.dp)
    ) {

        IconButton(
            onClick = {
                  // TODO add dialog bow to confirm
                  app.newModel()
            },
            modifier = AppTheme.toolBarIconModifier
        ) { Icon(FontAwesomeIcons.Solid.File, "New Model") }

        IconButton(
            onClick = {
                // Save
            },
            modifier = AppTheme.toolBarIconModifier
        ) { Icon(FontAwesomeIcons.Solid.Save, "Export Model") }

        IconButton(
            onClick = {
                app.scope.launch {
                    val file = openFileDialog("Open model", listOf("netbiodyn", "json"))
                    if (file != null) app.openPath(file)
                }
            },
            modifier = AppTheme.toolBarIconModifier
        ) {
            Icon(FontAwesomeIcons.Solid.FolderOpen, "Open Model")
        }

        Spacer(modifier = AppTheme.toolbarSpacerModifier)

        IconButton(
            onClick = { app.undo() },
            enabled = app.canUndo,
            modifier = AppTheme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Undo,
                contentDescription = null,
            )
        }

        IconButton(
            onClick = { app.redo() },
            enabled = app.canRedo,
            modifier = AppTheme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Redo,
                contentDescription = null,
            )
        }

        Spacer(modifier = AppTheme.toolbarSpacerModifier)

        IconButton(
            enabled = app.selection.filterIsInstance<Grain>().isNotEmpty(),
            onClick = {
                val grain = app.selection.filterIsInstance<Grain>().first()
                val grainId = grain.id
                val count = 30
                val newAgents = app.simulation.agents.toMutableList()
                repeat(count) {
                    // try at most 5 times to find a free place
                    for (i in 0 until 5) {
                        val index = Random.nextInt(app.simulation.dataSize)
                        if (newAgents[index] < 0) {
                            newAgents[index] = grainId
                            break
                        }
                    }
                }
                app.simulation = app.simulation.copy(agents = newAgents)
            },
            modifier = AppTheme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Random,
                contentDescription = null,
            )
        }
         IconButton(
            enabled = true,
            onClick = {
                val simulation = app.simulation
                app.simulation = simulation.copy(agents = List(simulation.agents.size) { -1 })
            },
            modifier = AppTheme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Trash,
                contentDescription = null,
            )
        }

        Spacer(modifier = AppTheme.toolbarSpacerModifier)


        IconButton(
            enabled = app.step > 0,
            onClick = { app.resetSimulation() },
            modifier = AppTheme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.FastBackward,
                contentDescription = null,
            )
        }

        IconButton(
            enabled = true,
            onClick = {
                if (app.running) app.stopSimulation()
                else scope.launch(Dispatchers.Unconfined) { app.startSimulation() }
            },
            modifier = AppTheme.toolBarIconModifier
        ) {
            Icon(
                imageVector = if (app.running) FontAwesomeIcons.Solid.Stop else FontAwesomeIcons.Solid.Play,
                contentDescription = null,
            )
        }

        IconButton(
            enabled = !app.running,
            onClick = { app.step() },
            modifier = AppTheme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.StepForward,
                contentDescription = null,
            )
        }

        Slider(
            value = app.speed,
            onValueChange = { app.speed = it },
            modifier = Modifier.width(100.dp)
        )

        Spacer(Modifier.width(2.dp))
        Text("${app.step}")
    }
}
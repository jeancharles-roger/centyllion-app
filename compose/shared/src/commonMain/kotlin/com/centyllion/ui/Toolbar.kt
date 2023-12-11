package com.centyllion.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centyllion.model.Grain
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import kotlin.random.Random

@Composable
fun ToolBar(appState: AppState) {

    TopAppBar(
        backgroundColor = appState.theme.colors.background,
        modifier = Modifier.height(40.dp)
    ) {

        IconButton(
            onClick = {
                  // TODO add dialog bow to confirm
                  appState.newModel()
            },
            modifier = appState.theme.toolBarIconModifier
        ) { Icon(FontAwesomeIcons.Solid.File, "New Model") }

        IconButton(
            onClick = {
                val files = newFileDialog(
                    window = appState.window,
                    title = "Select file to save",
                    preselectedFile = "${appState.model.name}.centyllion",
                    allowedExtensions = listOf(".netbiodyn"),
                    allowMultiSelection = false
                )
                if (files.isNotEmpty()) appState.setPath(files.first().toPath())
            },
            modifier = appState.theme.toolBarIconModifier
        ) { Icon(FontAwesomeIcons.Solid.Save, "Export Model") }

        IconButton(
            onClick = {
                val files = openFileDialog(appState.window, "Open model", listOf(".netbiodyn", ".json"), false)
                if (files.isNotEmpty()) appState.openPath(files.first().toPath())
            },
            modifier = appState.theme.toolBarIconModifier
        ) {
            Icon(FontAwesomeIcons.Solid.FolderOpen, "Open Model")
        }

        Spacer(modifier = appState.theme.toolbarSpacerModifier)

        IconButton(
            onClick = { appState.undo() },
            enabled = appState.canUndo,
            modifier = appState.theme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Undo,
                contentDescription = null,
            )
        }

        IconButton(
            onClick = { appState.redo() },
            enabled = appState.canRedo,
            modifier = appState.theme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Redo,
                contentDescription = null,
            )
        }

        Spacer(modifier = appState.theme.toolbarSpacerModifier)

        IconButton(
            enabled = appState.selection.filterIsInstance<Grain>().isNotEmpty(),
            onClick = {
                val grain = appState.selection.filterIsInstance<Grain>().first()
                val grainId = grain.id
                val count = 30
                val newAgents = appState.simulation.agents.toMutableList()
                repeat(count) {
                    // try at most 5 times to find a free place
                    for (i in 0 until 5) {
                        val index = Random.nextInt(appState.simulation.dataSize)
                        if (newAgents[index] < 0) {
                            newAgents[index] = grainId
                            break
                        }
                    }
                }
                appState.simulation = appState.simulation.copy(agents = newAgents)
            },
            modifier = appState.theme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Random,
                contentDescription = null,
            )
        }
         IconButton(
            enabled = true,
            onClick = {
                val simulation = appState.simulation
                appState.simulation = simulation.copy(agents = List(simulation.agents.size) { -1 })
            },
            modifier = appState.theme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Trash,
                contentDescription = null,
            )
        }

        Spacer(modifier = appState.theme.toolbarSpacerModifier)


        IconButton(
            enabled = appState.step > 0,
            onClick = { appState.resetSimulation() },
            modifier = appState.theme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.FastBackward,
                contentDescription = null,
            )
        }

        IconButton(
            enabled = true,
            onClick = { appState.startStopSimulation() },
            modifier = appState.theme.toolBarIconModifier
        ) {
            Icon(
                imageVector = if (appState.running) FontAwesomeIcons.Solid.Stop else FontAwesomeIcons.Solid.Play,
                contentDescription = null,
            )
        }

        IconButton(
            enabled = !appState.running,
            onClick = { appState.step() },
            modifier = appState.theme.toolBarIconModifier
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.StepForward,
                contentDescription = null,
            )
        }

        Slider(
            value = appState.speed,
            onValueChange = { appState.speed = it },
            modifier = Modifier.width(100.dp)
        )

        Spacer(Modifier.width(2.dp))
        Text("${appState.step}")
    }
}
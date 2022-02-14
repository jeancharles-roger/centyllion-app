package com.centyllion.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

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
                    allowedExtensions = listOf(".centyllion"),
                    allowMultiSelection = false
                )
                if (files.isNotEmpty()) appState.setPath(files.first().toPath())
            },
            modifier = appState.theme.toolBarIconModifier
        ) { Icon(FontAwesomeIcons.Solid.Save, "Export Model") }

        IconButton(
            onClick = {
                val files = openFileDialog(appState.window, "Open model", listOf(".centyllion"), false)
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

    }
}
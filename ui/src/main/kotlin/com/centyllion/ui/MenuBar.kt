package com.centyllion.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar

@Composable
fun FrameWindowScope.MenuBar(appState: AppState) {
    MenuBar {
        Menu("Project", 'p') {
            Item(text = "New model", mnemonic = 'n') {
                // TODO add dialog bow to confirm
                appState.newModel()
            }
            Item(text = "Save model\u2026", mnemonic = 's') {
                val files = newFileDialog(
                    window = appState.window,
                    title = "Select file to save",
                    preselectedFile = "${appState.model}.components",
                    allowedExtensions = listOf(".components"),
                    allowMultiSelection = false,
                )
                if (files.isNotEmpty()) appState.setPath(files.first().toPath())
            }
            Item(text = "Open model\u2026", mnemonic = 'o') {
                val files = openFileDialog(appState.window, "Open model", listOf(".components"), false)
                if (files.isNotEmpty()) appState.openPath(files.first().toPath())
            }
        }

        Menu("Edit", 'e') {
            Item(text = "Undo", enabled = appState.canUndo, mnemonic = 'u') { appState.undo() }
            Item(text = "Redo", enabled = appState.canRedo, mnemonic = 'r') { appState.redo() }
            Separator()
        }

        Menu("View", 'v') {
            Item(text = "Clear logs", mnemonic = 'c') { appState.clearLogs() }
            Separator()
        }
    }
}
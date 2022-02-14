package com.centyllion.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar

@Composable
fun FrameWindowScope.MenuBar(appState: AppState) {
    MenuBar {
        Menu(appState.locale.i18n("Project"), 'p') {
            Item(appState.locale.i18n("New model"), mnemonic = 'n') {
                // TODO add dialog bow to confirm
                appState.newModel()
            }
            Item(appState.locale.i18n("Save model\u2026"), mnemonic = 's') {
                val files = newFileDialog(
                    window = appState.window,
                    title = "Select file to save",
                    preselectedFile = "${appState.model}.centyllion",
                    allowedExtensions = listOf(".centyllion"),
                    allowMultiSelection = false,
                )
                if (files.isNotEmpty()) appState.setPath(files.first().toPath())
            }
            Item(text = appState.locale.i18n("Open model\u2026"), mnemonic = 'o') {
                val files = openFileDialog(appState.window, "Open model", listOf(".centyllion"), false)
                if (files.isNotEmpty()) appState.openPath(files.first().toPath())
            }
        }

        Menu(appState.locale.i18n("Edit"), 'e') {
            Item(text = appState.locale.i18n("Undo"), enabled = appState.canUndo, mnemonic = 'u') { appState.undo() }
            Item(text = appState.locale.i18n("Redo"), enabled = appState.canRedo, mnemonic = 'r') { appState.redo() }
            Separator()
        }

        Menu(appState.locale.i18n("View"), 'v') {
            Item(text = appState.locale.i18n("Clear logs"), mnemonic = 'c') { appState.clearLogs() }
            Separator()

            val locales = appState.locales
            locales.locales.zip(locales.labels).forEach { (id, label) ->
                CheckboxItem(text = label, checked = id == appState.locale.name)
                    { appState.locale = locales.locale(id) }
            }
        }
    }
}
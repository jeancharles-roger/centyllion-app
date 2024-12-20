package com.centyllion.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.centyllion.i18n.Locales

@Composable
fun FrameWindowScope.MenuBar(appState: AppState) {
    MenuBar {
        Menu(appState.locale.i18n("Project"), 'p') {
            Item(appState.locale.i18n("New model"), mnemonic = 'n') {
                // TODO add dialog bow to confirm
                appState.newModel()
            }
            Item(appState.locale.i18n("Save model\u2026"), mnemonic = 's') {
                appState.window?.let { window ->
                    val files = newFileDialog(
                        window = window,
                        title = "Select file to save",
                        preselectedFile = "${appState.modelAndSimulation}.netbiodyn",
                        allowedExtensions = listOf(".netbiodyn"),
                        allowMultiSelection = false,
                    )
                    if (files.isNotEmpty()) appState.setPath(files.first().toPath())
                }
            }

            Item(text = appState.locale.i18n("Open model\u2026"), mnemonic = 'o') {
                appState.window?.let { window ->
                    val files = openFileDialog(window, "Open model", listOf(".netbiodyn", ".json"), false)
                    if (files.isNotEmpty()) appState.openPath(files.first().toPath())
                }
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
            Locales.locales.forEach { locale ->
                CheckboxItem(text = locale.label, checked = locale.name == appState.locale.name)
                    { appState.locale = locale }
            }

        }
    }
}
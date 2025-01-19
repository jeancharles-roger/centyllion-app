package com.centyllion.ui

import JvmAppState
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.centyllion.i18n.Locales
import kotlinx.coroutines.launch

@Composable
fun FrameWindowScope.MenuBar(appState: JvmAppState) {
    MenuBar {
        Menu(appState.locale.i18n("Project"), 'p') {
            Item(appState.locale.i18n("New model"), mnemonic = 'n') {
                // TODO add dialog bow to confirm
                appState.newModel()
            }
            Item(appState.locale.i18n("Save model\u2026"), mnemonic = 's') {
                appState.scope.launch {
                    val file = openFileDialog(
                        title = "Select file to save",
                        allowedExtensions = listOf("netbiodyn"),
                    )
                    if (file != null) appState.setPath(file)
                }
            }

            Item(text = appState.locale.i18n("Open model\u2026"), mnemonic = 'o') {
                appState.scope.launch {
                    val file = openFileDialog("Open model", listOf("netbiodyn", "json"))
                    if (file != null) appState.openPath(file)
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
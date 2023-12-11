package com.centyllion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.window.singleWindowApplication
import com.centyllion.ui.tabs.Tabs
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.nio.file.Path

fun main() = singleWindowApplication {
    val scope = rememberCoroutineScope()
    val appState = remember { AppState(window, scope, mutableStateOf<Path?>(null)) }
    MenuBar(appState)
    App(appState)
}


@Composable
fun App(appState: AppState) {
    MaterialTheme(colors = themeColors) {

        appState.currentDialog?.let { currentDialog ->
            Dialog(
                title = appState.locale.i18n(currentDialog.titleKey),
                onCloseRequest = { appState.currentDialog = null },
                state = rememberDialogState(position = WindowPosition(Alignment.Center))
            ) {
                currentDialog.content(appState)
            }
        }

        Column {
            ToolBar(appState = appState)
            MainView(appState)
        }
    }
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
private fun MainView(appState: AppState) {
    HorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(.25f),
        modifier = Modifier.background(appState.theme.colors.background)
    ) {
        first {
            ComponentTree(appState)
        }
        second {
            Tabs(
                appContext = appState,
                tabs = appState.centerTabs,
                selected = appState.centerSelectedTab,
                onTabSelection = { appState.centerSelectedTab = it }
            )
        }
        horizontalSplitter()
    }
}

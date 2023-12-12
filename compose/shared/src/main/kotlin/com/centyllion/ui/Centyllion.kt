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
import androidx.compose.ui.window.DialogWindow
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
            DialogWindow(
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
    val west = 1f
    val center = 2f
    val east = 1f
    val total = west + center + east


    HorizontalSplitPane(
        splitPaneState = rememberSplitPaneState(west/total),
        modifier = Modifier.background(appState.theme.colors.background)
    ) {
        first {
            ComponentTree(appState)
        }
        second {

            HorizontalSplitPane(
                splitPaneState = rememberSplitPaneState(center/(total-west)),
                modifier = Modifier.background(appState.theme.colors.background)
            ) {
                first {
                    Tabs(
                        appContext = appState,
                        tabs = appState.centerTabs,
                        selected = appState.centerSelectedTab,
                        onTabSelection = { appState.centerSelectedTab = it }
                    )
                }
                second {
                    Tabs(
                        appContext = appState,
                        tabs = appState.eastTabs,
                        selected = appState.eastSelectedTab,
                        onTabSelection = { appState.eastSelectedTab = it }
                    )
                }
                horizontalSplitter()
            }
        }
        horizontalSplitter()
    }
}

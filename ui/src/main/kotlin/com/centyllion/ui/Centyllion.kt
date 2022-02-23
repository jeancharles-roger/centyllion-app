package com.centyllion.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.centyllion.ui.tabs.Tabs
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.nio.file.Path

fun main() = application {
    Window(
        onCloseRequest = { exitApplication() },
    ) {
        val scope = rememberCoroutineScope()
        val appState = remember { AppState(window, scope, mutableStateOf<Path?>(null)) }
        MenuBar(appState)
        App(appState)
    }
}


@Composable
@Preview
fun App(appState: AppState, ) {
    MaterialTheme(colors = themeColors) {
        Column {
            ToolBar(appState = appState)
            MainView(appState)
        }
    }
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
private fun MainView(appState: AppState) {
    VerticalSplitPane(
        splitPaneState = rememberSplitPaneState(.8f),
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        first {
            HorizontalSplitPane(splitPaneState = rememberSplitPaneState(.2f)) {
                first {
                    ComponentTree(appState)
                }
                second {
                    Tabs(appState, appState.centerTabs, appState.centerSelectedTab) {
                        appState.centerSelectedTab = it
                    }
                }
                horizontalSplitter()
            }
        }
        second {
            Tabs(appState, appState.southTabs, appState.southSelectedTab) {
                appState.southSelectedTab = it
            }
        }
        verticalSplitter()
    }
}
package com.centyllion.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centyllion.ui.tabs.Tabs

@Composable
fun App(appState: AppState) {
    MaterialTheme(colors = themeColors) {
        Column {
            ToolBar(app = appState)
            MainView(appState)
        }
    }
}

@Composable
private fun MainView(appState: AppState) {
    Row {
        ComponentTree(
            modifier = Modifier.weight(1f).border(2.dp, AppTheme.colors.onPrimary),
            appState = appState
        )

        Tabs(
            modifier = Modifier.weight(2f).border(2.dp, AppTheme.colors.onPrimary),
            appContext = appState,
            tabs = appState.centerTabs,
            selected = appState.centerSelectedTab,
            onTabSelection = { appState.centerSelectedTab = it }
        )

        Tabs(
            modifier = Modifier.weight(1f).border(2.dp, AppTheme.colors.onPrimary),
            appContext = appState,
            tabs = appState.eastTabs,
            selected = appState.eastSelectedTab,
            onTabSelection = { appState.eastSelectedTab = it }
        )
    }

}

package com.centyllion.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.ui.AppContext
import com.centyllion.ui.AppTheme

sealed interface Tab {
    val nameKey: String
    val icon: ImageVector

    @Composable
    fun header(appContext: AppContext) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                tint = LocalContentColor.current,
                contentDescription = appContext.locale.i18n(nameKey),
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = appContext.locale.i18n(nameKey),
                color = LocalContentColor.current,
                fontSize = 14.sp,
            )
        }

    }

    @Composable
    fun content(app: AppContext)
}

@Composable
fun Tabs(
    modifier: Modifier, appContext: AppContext, tabs: List<Tab>,
    selected: Tab, onTabSelection: (Tab) -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(top = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (tab in tabs) {
                Tab(appContext, tab, selected == tab) { onTabSelection(tab) }
            }
        }

        Spacer(
            modifier = Modifier
                .background(Color.Gray.copy(alpha = .7f))
                .fillMaxWidth()
                .height(2.dp)
        )

        Row(Modifier.weight(1f).padding(all = 4.dp)) {
            selected.content(appContext)
        }
    }
}

@Composable
fun RowScope.Tab(appContext: AppContext, tab: Tab, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .weight(1f)
            .background(if (selected) AppTheme.colors.background else AppTheme.backgroundMedium)
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        tab.header(appContext)
    }
}
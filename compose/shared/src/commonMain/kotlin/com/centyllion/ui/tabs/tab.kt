package com.centyllion.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.ui.AppContext

sealed interface Tab {
    val nameKey: String
    val icon: ImageVector

    @Composable
    fun header(appContext: AppContext) {
        Icon(
            imageVector = icon,
            tint = LocalContentColor.current,
            contentDescription = appContext.locale.i18n(nameKey),
            modifier = Modifier
                .size(24.dp)
                .padding(4.dp)
        )

        Text(
            appContext.locale.i18n(nameKey),
            color = LocalContentColor.current,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    @Composable
    fun content(appContext: AppContext)
}

@Composable
fun Tabs(
    appContext: AppContext, tabs: List<Tab>,
    selected: Tab, onTabSelection: (Tab) -> Unit
) {
    Box {
        Column {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(color = appContext.theme.backgroundLight)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (tab in tabs) {
                    Tab(appContext, tab, selected == tab) { onTabSelection(tab) }
                }
            }
            val spacerModifier = Modifier
                .background(Color.Gray.copy(alpha = .7f))
                .fillMaxWidth()
                .height(2.dp)

            Spacer(modifier = spacerModifier)

            Row(Modifier.padding(all = 4.dp)) {
                selected.content(appContext)
            }
        }
    }
}

@Composable
fun Tab(appContext: AppContext, tab: Tab, selected: Boolean, onClick: () -> Unit) {
    Surface(color = if (selected) appContext.theme.backgroundDark else appContext.theme.backgroundMedium) {
        Row(
            Modifier
                .clickable(remember(::MutableInteractionSource), indication = null, onClick = onClick)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { tab.header(appContext) }
    }
}
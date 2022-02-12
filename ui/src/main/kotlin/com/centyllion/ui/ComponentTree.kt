package com.centyllion.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.model.ModelElement

@Composable
fun ComponentTree(appState: AppState) {
    Box(Modifier.padding(4.dp)) {
        val listState = rememberLazyListState()

        LazyColumn(state = listState) {
            item {
                TreeItem(appState, appState.model)
            }

            items(appState.model.grains) {
                TreeItem(appState, it)
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = listState)
        )
    }
}


@Composable
fun TreeItem(
    appState: AppState,
    element: ModelElement,
) {
    /*
    val leaf = when (element) {
        is Project -> true
        is Component -> element.components.isEmpty() && element.connection.provided.isEmpty()
        else -> true
    }
    */
    val selected = appState.selection.contains(element)
    /*
    val expanded = appState.componentExpanded.contains(element.id)

    val diagnostic = appState.diagnostics.filter { it.source == element.id }.maxByOrNull { it.severity }
    val severity = diagnostic?.severity
    */

    val colorWithError = when {
        //severity != null -> appState.theme.severityColor(severity)
        selected -> appState.theme.colors.onPrimary
        else -> appState.theme.colors.onSurface
    }

    val color = when {
        selected -> appState.theme.colors.onPrimary
        else -> appState.theme.colors.onSurface
    }

    Row(
        modifier = Modifier
            .background(if (selected) appState.theme.colors.primary else appState.theme.colors.surface)
            .clickable { appState.selection = listOf(element) }
            .padding(6.dp)
    ) {
        /*
        Spacer(modifier = Modifier.width(16.dp * parents.size))

        val stateIcon = when {
            leaf -> Icons.TreeLeaf
            expanded -> Icons.TreeExpanded
            else -> Icons.TreeFolded
        }

        Icon(
            imageVector = stateIcon, contentDescription = null,
            tint = color,
            modifier = Modifier
                .padding(2.dp)
                .height(16.dp)
                .align(Alignment.CenterVertically)
                .clickable {
                    // expand or collapse item
                    if (!leaf) {
                        if (expanded) appState.componentExpanded -= element.id
                        else appState.componentExpanded += element.id
                    }
                }
        )

        Spacer(modifier = Modifier.width(8.dp))
        */

        Icon(
            imageVector = element.icon, contentDescription = null,
            tint = colorWithError,
            modifier = Modifier.height(20.dp).align(Alignment.CenterVertically),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            color = color,
            text = AnnotatedString(element.name),
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.weight(1F).align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(8.dp))

        /*
        diagnostic?.let {
            Icon(
                imageVector = it.severity.icon, contentDescription = null,
                tint = appState.theme.severityColor(it.severity),
                modifier = Modifier
                    .height(16.dp).align(Alignment.CenterVertically)
                    .clickable { appState.selectedDiagnostic = it }
            )
        }
         */
    }

    /*
    if (element is Component && expanded) {
        (element.components + element.connection.provided).forEach {
            TreeItem(appState, parents + element, it)
        }
        Divider()
    }
     */
}

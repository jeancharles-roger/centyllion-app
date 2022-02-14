package com.centyllion.ui.tabs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.centyllion.model.Grain
import com.centyllion.ui.AppContext
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ProjectDiagram

object PropertiesTab : Tab {
    override val nameKey = "Properties"
    override val icon = FontAwesomeIcons.Solid.ProjectDiagram

    @Composable
    override fun content(appContext: AppContext) {
        Box {
            val lazyListState = rememberLazyListState()
            LazyColumn(state = lazyListState) {
                val selectedComponents = appContext.selection
                if (selectedComponents.isNotEmpty()) {
                    items(selectedComponents) { element ->
                        when (element) {
                            is Grain -> Row { GrainEdit(appContext, element) }
                        }
                    }
                } else {
                    item {
                        Surface(appContext.theme.surfaceModifier) {
                            Text(appContext.locale.i18n("Select a element to edit it"))
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = lazyListState)
            )
        }
    }
}

@Composable
fun GrainEdit(appContext: AppContext, grain: Grain) {

}
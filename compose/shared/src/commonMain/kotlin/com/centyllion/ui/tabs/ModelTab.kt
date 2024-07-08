package com.centyllion.ui.tabs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.centyllion.model.Behaviour
import com.centyllion.model.Field
import com.centyllion.model.Grain
import com.centyllion.model.colorNames
import com.centyllion.ui.AppContext
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Boxes
import compose.icons.fontawesomeicons.solid.SquareFull

object ModelTab : Tab {
    override val nameKey = "Model"
    override val icon = FontAwesomeIcons.Solid.Boxes

    @Composable
    override fun content(appContext: AppContext) {
        Box {
            val lazyListState = rememberLazyListState()
            LazyColumn(state = lazyListState) {
                val selectedComponents = appContext.selection
                if (selectedComponents.isNotEmpty()) {
                    items(selectedComponents) { element ->
                        when (element) {
                            is Behaviour -> Row { BehaviourEdit(appContext, element) }
                            is Grain -> Row { GrainEdit(appContext, element) }
                            is Field -> Row { FieldEdit(appContext, element) }
                            else -> Row { }
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
fun ColoredSquare(color: String) {
    val squareColor = colorNames[color]
        ?.let { Color(it.first, it.second, it.third) }
        ?: Color.Red

    Icon(
        imageVector = FontAwesomeIcons.Solid.SquareFull,
        contentDescription = null, tint = squareColor,
        modifier = Modifier.height(20.dp),
    )
}

@Composable
fun SimpleIcon(icon: ImageVector, color: Color = Color.Black, ) {
    Icon(
        imageVector = icon, contentDescription = null,
        modifier = Modifier.height(20.dp), tint = color
    )
}
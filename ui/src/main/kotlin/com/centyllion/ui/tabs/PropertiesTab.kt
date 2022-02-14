package com.centyllion.ui.tabs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.centyllion.model.Grain
import com.centyllion.model.colorNameList
import com.centyllion.model.colorNames
import com.centyllion.ui.*
import compose.icons.AllIcons
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.AllIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ProjectDiagram
import compose.icons.fontawesomeicons.solid.QuestionCircle
import compose.icons.fontawesomeicons.solid.SquareFull
import kotlin.math.roundToInt

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
    Surface(appContext.theme.surfaceModifier) {
        Box(modifier = Modifier.padding(4.dp)) {
            Column {
                MainTitleRow(appContext.locale.i18n("Grain"))

                SingleLineTextEditRow(appContext, grain, "Name", grain.name) {
                    appContext.model = appContext.model.updateGrain(grain, grain.copy(name = it))
                }

                MultiLineTextEditRow(appContext, grain, "Description", grain.description) {
                    appContext.model = appContext.model.updateGrain(grain, grain.copy(description = it))
                }

                TitleRow(appContext.locale.i18n("Display"))

                ComboRow(appContext, grain, "Icon", grain.iconName,
                    FontAwesomeIcons.Solid.AllIcons.map { it.name }, { iconName ->
                        val icon = FontAwesomeIcons.AllIcons.find { it.name == iconName }
                        if (icon != null) Icon(icon) else Icon(FontAwesomeIcons.Solid.QuestionCircle, Color.Red)
                        Spacer(Modifier.width(4.dp))
                        Text(iconName)
                    }
                ) {
                    val new = grain.copy(icon = it)
                    appContext.model = appContext.model.updateGrain(grain, new)
                }

                ComboRow(appContext, grain, "Color", grain.color,
                    colorNameList, {
                        ColoredSquare(it)
                        Spacer(Modifier.width(4.dp))
                        Text(it)
                    }
                ) {
                    val new = grain.copy(color = it)
                    appContext.model = appContext.model.updateGrain(grain, new)
                }

                // TODO filter problems
                CustomRow(appContext, emptyList()) {
                    Label(appContext.locale.i18n("Size"))
                    Label(((grain.size * 10.0).roundToInt()/10.0).toString())
                    Slider(
                        value = grain.size.toFloat(),
                        valueRange = 0f.rangeTo(5f),
                        steps = 60,
                        onValueChange = {
                            val new = grain.copy(size = it.toDouble())
                            appContext.model = appContext.model.updateGrain(grain, new)
                        }
                    )
                }

                CustomRow(appContext, emptyList()) {
                    Label(appContext.locale.i18n("Invisible"))
                    Checkbox(
                        checked = grain.invisible,
                        colors = appContext.theme.checkboxColors(),
                        onCheckedChange = {
                            val new = grain.copy(invisible = it)
                            appContext.model = appContext.model.updateGrain(grain, new)
                        }
                    )
                }

            }
        }
    }
}


@Composable
private fun ColoredSquare(color: String) {
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
private fun Icon(icon: ImageVector, color: Color = Color.Black) {
    Icon(
        imageVector = icon, contentDescription = null,
        modifier = Modifier.height(20.dp), tint = color
    )
}
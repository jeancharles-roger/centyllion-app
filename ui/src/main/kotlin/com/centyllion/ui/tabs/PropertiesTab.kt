package com.centyllion.ui.tabs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.centyllion.model.*
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

                IntEditRow(appContext, grain, "Half-life", grain.halfLife) {
                    appContext.model = appContext.model.updateGrain(grain, grain.copy(halfLife = it))
                }

                DoubleEditRow(appContext, grain, "Speed", grain.movementProbability,
                    trailingIcon = {
                        Row {
                            firstDirections.forEachIndexed { index, direction ->
                                val selected = grain.allowedDirection.contains(direction)
                                val shape = when (index) {
                                    0 -> RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                                    firstDirections.size - 1 -> RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                                    else -> RectangleShape
                                }
                                Icon(
                                    imageVector = direction.icon, contentDescription = null,
                                    tint = if (selected) appContext.theme.colors.onPrimary else appContext.theme.colors.primary,
                                    modifier = Modifier.size(28.dp)
                                        .background(
                                            color = if (selected) appContext.theme.colors.primary else appContext.theme.colors.onPrimary,
                                            shape = shape
                                        )
                                        .padding(5.dp, 2.dp)
                                        .clickable {
                                            val directions = if (selected) grain.allowedDirection - direction else grain.allowedDirection + direction
                                           appContext.model = appContext.model.updateGrain(grain, grain.copy(allowedDirection = directions))
                                        },
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            extendedDirections.forEachIndexed { index, direction ->
                                val selected = grain.allowedDirection.contains(direction)
                                val shape = when (index) {
                                    0 -> RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                                    extendedDirections.size - 1 -> RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                                    else -> RectangleShape
                                }
                                Icon(
                                    imageVector = direction.icon, contentDescription = null,
                                    tint = if (selected) appContext.theme.colors.onPrimary else appContext.theme.colors.primary,
                                    modifier = Modifier.size(28.dp)
                                        .background(
                                            color = if (selected) appContext.theme.colors.primary else appContext.theme.colors.onPrimary,
                                            shape = shape
                                        )
                                        .padding(5.dp, 2.dp)
                                        .clickable {
                                            val directions = if (selected) grain.allowedDirection - direction else grain.allowedDirection + direction
                                            appContext.model = appContext.model.updateGrain(grain, grain.copy(allowedDirection = directions))
                                        },
                                )
                            }

                            Spacer(Modifier.width(12.dp))
                        }
                    }
                ) {
                    appContext.model = appContext.model.updateGrain(grain, grain.copy(movementProbability = it))
                }

                TitleRow(appContext.locale.i18n("Display"))

                ComboRow(appContext, grain, "Icon", grain.iconName,
                    FontAwesomeIcons.Solid.AllIcons.map { it.name }, { iconName ->
                        val icon = FontAwesomeIcons.AllIcons.find { it.name == iconName }
                        if (icon != null) SimpleIcon(icon) else SimpleIcon(FontAwesomeIcons.Solid.QuestionCircle, Color.Red)
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
                    Text(appContext.locale.i18n("Size"), Modifier.align(Alignment.CenterVertically))
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
                    Text(appContext.locale.i18n("Invisible"), Modifier.align(Alignment.CenterVertically))
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
private fun SimpleIcon(icon: ImageVector, color: Color = Color.Black, ) {
    Icon(
        imageVector = icon, contentDescription = null,
        modifier = Modifier.height(20.dp), tint = color
    )
}
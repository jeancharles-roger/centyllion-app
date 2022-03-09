package com.centyllion.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.centyllion.model.Grain
import com.centyllion.model.colorNameList
import com.centyllion.model.extendedDirections
import com.centyllion.model.firstDirections
import com.centyllion.ui.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.QuestionCircle

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
                SpeedAndDirection(appContext, grain)

                GrainDisplay(appContext, grain)

                FieldInteractions(appContext, grain)
            }
        }
    }
}

@Composable
private fun SpeedAndDirection(appContext: AppContext, grain: Grain) {
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
                        imageVector = direction.icon(), contentDescription = null,
                        tint = if (selected) appContext.theme.colors.onPrimary else appContext.theme.colors.primary,
                        modifier = Modifier.size(28.dp)
                            .background(
                                color = if (selected) appContext.theme.colors.primary else appContext.theme.colors.onPrimary,
                                shape = shape
                            )
                            .padding(5.dp, 2.dp)
                            .clickable {
                                val directions =
                                    if (selected) grain.allowedDirection - direction else grain.allowedDirection + direction
                                appContext.model = appContext.model.updateGrain(
                                    grain,
                                    grain.copy(allowedDirection = directions)
                                )
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
                        imageVector = direction.icon(), contentDescription = null,
                        tint = if (selected) appContext.theme.colors.onPrimary else appContext.theme.colors.primary,
                        modifier = Modifier.size(28.dp)
                            .background(
                                color = if (selected) appContext.theme.colors.primary else appContext.theme.colors.onPrimary,
                                shape = shape
                            )
                            .padding(5.dp, 2.dp)
                            .clickable {
                                val directions =
                                    if (selected) grain.allowedDirection - direction else grain.allowedDirection + direction
                                appContext.model = appContext.model.updateGrain(
                                    grain,
                                    grain.copy(allowedDirection = directions)
                                )
                            },
                    )
                }

                Spacer(Modifier.width(12.dp))
            }
        }
    ) {
        appContext.model = appContext.model.updateGrain(grain, grain.copy(movementProbability = it))
    }
}

@Composable
private fun GrainDisplay(appContext: AppContext, grain: Grain) {
    TitleRow(appContext.locale.i18n("Display"))

    ComboRow(appContext, grain, "Icon", grain.iconName,
        allIcons.keys.toList(), { iconName ->
            val icon = allIcons[iconName]
            if (icon != null) SimpleIcon(icon) else SimpleIcon(
                FontAwesomeIcons.Solid.QuestionCircle,
                Color.Red
            )
            Spacer(Modifier.width(4.dp))
            Text(iconName)
        }, lazy = true
    ) {
        val new = grain.copy(icon = it)
        appContext.model = appContext.model.updateGrain(grain, new)
    }

    ComboRow(appContext, grain, "Color", grain.color,
        colorNameList, {
            ColoredSquare(it)
            Spacer(Modifier.width(4.dp))
            Text(it)
        }, lazy = true
    ) {
        val new = grain.copy(color = it)
        appContext.model = appContext.model.updateGrain(grain, new)
    }

    /*
    CustomRow(appContext, problems(appContext, grain, "Size")) {
        Text(appContext.locale.i18n("Size"), Modifier.align(Alignment.CenterVertically))
        Label(grain.size.toFixedString(1))
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
     */

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

@Composable
private fun FieldInteractions(appContext: AppContext, grain: Grain) {
    if (appContext.model.fields.isNotEmpty()) {
        TitleRow(appContext.locale.i18n("Field Interactions"))

        Row {
            Column(Modifier.weight(.15f)) {  }
            Column(Modifier.weight(.28f)) { Text(appContext.locale.i18n("Productions"), Modifier.align(Alignment.CenterHorizontally)) }
            Column(Modifier.weight(.28f)) { Text(appContext.locale.i18n("Influences"), Modifier.align(Alignment.CenterHorizontally)) }
            Column(Modifier.weight(.28f)) { Text(appContext.locale.i18n("Permeability"), Modifier.align(Alignment.CenterHorizontally)) }
        }
        appContext.model.fields.forEach { field ->
            Row {
                Column(Modifier.weight(.15f).align(Alignment.CenterVertically)) { Text(field.name) }
                Column(Modifier.weight(.28f).align(Alignment.CenterVertically)) {
                    Slider(
                        value = grain.fieldProductions[field.id] ?: 0f,
                        valueRange = (-1f).rangeTo(1f),
                        steps = 200,
                        onValueChange = {
                            val updated = grain.fieldProductions.toMutableMap().apply { this[field.id] = it }
                            val new = grain.copy(fieldProductions = updated)
                            appContext.model = appContext.model.updateGrain(grain, new)
                        }
                    )
                }
                Column(Modifier.weight(.28f).align(Alignment.CenterVertically)) {
                    Slider(
                        value = grain.fieldInfluences[field.id] ?: 0f,
                        valueRange = (-1f).rangeTo(1f),
                        steps = 200,
                        onValueChange = {
                            val updated = grain.fieldInfluences.toMutableMap().apply { this[field.id] = it }
                            val new = grain.copy(fieldInfluences = updated)
                            appContext.model = appContext.model.updateGrain(grain, new)
                        }
                    )
                }
                Column(Modifier.weight(.28f).align(Alignment.CenterVertically)) {
                    Slider(
                        value = grain.fieldPermeable[field.id] ?: 0f,
                        valueRange = 0f.rangeTo(1f),
                        steps = 100,
                        onValueChange = {
                            val updated = grain.fieldPermeable.toMutableMap().apply { this[field.id] = it }
                            val new = grain.copy(fieldPermeable = updated)
                            appContext.model = appContext.model.updateGrain(grain, new)
                        }
                    )
                }
            }
        }
    }
}
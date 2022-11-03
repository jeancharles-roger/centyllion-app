package com.centyllion.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.model.Grain
import com.centyllion.model.colorNameList
import com.centyllion.ui.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.QuestionCircle
import kotlin.math.abs

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
        trailingRatio = .35f,
        trailingContent = {
            Directions(appContext, grain.allowedDirection) {
                appContext.model = appContext.model.updateGrain(
                    grain, grain.copy(allowedDirection = it)
                )
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
        allIcons.keys.toList(), valueContent = { iconName ->
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
        colorNameList, valueContent = {
            ColoredSquare(it)
            Spacer(Modifier.width(4.dp))
            Text(it)
        }, lazy = true
    ) {
        val new = grain.copy(color = it)
        appContext.model = appContext.model.updateGrain(grain, new)
    }

    CheckRow(appContext, grain, "Invisible", grain.invisible) {
        val new = grain.copy(invisible = it)
        appContext.model = appContext.model.updateGrain(grain, new)
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
                    fieldInteraction(appContext, grain.fieldProductions[field.id] ?: 0f) {
                        val value = if (abs(it) < .1f) 0f else it
                        val updated = grain.fieldProductions.toMutableMap().apply { this[field.id] = value }
                        val new = grain.copy(fieldProductions = updated)
                        appContext.model = appContext.model.updateGrain(grain, new)
                    }
                }
                Column(Modifier.weight(.28f).align(Alignment.CenterVertically)) {
                    fieldInteraction(appContext, grain.fieldInfluences[field.id] ?: 0f) {
                        val value = if (abs(it) < .1f) 0f else it
                        val updated = grain.fieldInfluences.toMutableMap().apply { this[field.id] = value }
                        val new = grain.copy(fieldInfluences = updated)
                        appContext.model = appContext.model.updateGrain(grain, new)
                    }
                }
                Column(Modifier.weight(.28f).align(Alignment.CenterVertically)) {
                    fieldInteraction(appContext, grain.fieldPermeable[field.id] ?: 0f, 0f.rangeTo(1f)) {
                        val updated = grain.fieldPermeable.toMutableMap().apply { this[field.id] = it }
                        val new = grain.copy(fieldPermeable = updated)
                        appContext.model = appContext.model.updateGrain(grain, new)
                    }
                }
            }
        }
    }
}

@Composable
fun fieldInteraction(
    appContext: AppContext,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = (-1f).rangeTo(1f),
    onValueChange: (Float) -> Unit,
) {
    Row {
        val colors = when {
            value < 0f -> appContext.theme.sliderNegative()
            value > 0f -> appContext.theme.sliderPositive()
            else -> appContext.theme.sliderNeutral()
        }
        Text(
            value.toFixedString(2),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterVertically).width(32.dp),
        )
        Slider(
            value = value,
            valueRange = valueRange,
            colors = colors,
            onValueChange = onValueChange
        )
    }
}
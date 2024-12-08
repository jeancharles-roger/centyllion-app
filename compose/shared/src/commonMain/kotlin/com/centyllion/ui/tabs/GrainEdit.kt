package com.centyllion.ui.tabs

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.GlobalScope
import java.nio.file.Path
import kotlin.math.abs


@Composable
@Preview
fun Preview() {
    val app = AppState(null, GlobalScope, mutableStateOf(Path.of("/Users/charlie/Downloads/phalenes1.netbiodyn")))
    //GrainItem(app, app.model.grains.first())
    GrainEdit(app, app.model.grains.first())

    //BehaviourItem(app, app.model.behaviours.first())

    //FieldItem(app, app.model.fields.first())
}

@Composable
fun GrainEdit(app: AppContext, grain: Grain) {
    Properties(app, "Grain") {
        SingleLineTextEditRow(app, grain, "Name", grain.name) {
            app.modelAndSimulation = app.modelAndSimulation.updateGrain(grain, grain.copy(name = it))
        }

        MultiLineTextEditRow(app, grain, "Description", grain.description) {
            app.modelAndSimulation = app.modelAndSimulation.updateGrain(grain, grain.copy(description = it))
        }

        IntEditRow(app, grain, "Half-life", grain.halfLife) {
            app.modelAndSimulation = app.modelAndSimulation.updateGrain(grain, grain.copy(halfLife = it))
        }

        SpeedAndDirection(app, grain)

        GrainDisplay(app, grain)

        FieldInteractions(app, grain)
    }
}

@Composable
private fun SpeedAndDirection(appContext: AppContext, grain: Grain) {
    DoubleEditRow(
        appContext, grain, "Speed", grain.movementProbability,
        trailingRatio = .35f,
        trailingContent = {
            Directions(appContext, grain.allowedDirection) {
                appContext.modelAndSimulation = appContext.modelAndSimulation.updateGrain(
                    grain, grain.copy(allowedDirection = it)
                )
            }
        }
    ) {
        appContext.modelAndSimulation =
            appContext.modelAndSimulation.updateGrain(grain, grain.copy(movementProbability = it))
    }
}

@Composable
private fun GrainDisplay(appContext: AppContext, grain: Grain) {
    TitleRow(appContext.locale.i18n("Display"))

    ComboRow(
        appContext, grain, "Icon", grain.iconName,
        allIcons.keys.toList(), valueContent = { iconName ->
            val icon = allIcons[iconName]
            if (icon != null) SimpleIcon(icon) else SimpleIcon(
                FontAwesomeIcons.Solid.QuestionCircle,
                Color.Red
            )
            Text(text = iconName, fontSize = 15.sp)
        }, lazy = true
    ) {
        val new = grain.copy(icon = it)
        appContext.modelAndSimulation = appContext.modelAndSimulation.updateGrain(grain, new)
    }

    ComboRow(
        appContext, grain, "Color", grain.color,
        colorNameList, valueContent = {
            ColoredSquare(it)
            Text(text = it, fontSize = 15.sp)
        }, lazy = true
    ) {
        val new = grain.copy(color = it)
        appContext.modelAndSimulation = appContext.modelAndSimulation.updateGrain(grain, new)
    }

    CheckRow(appContext, grain, "Invisible", grain.invisible) {
        val new = grain.copy(invisible = it)
        appContext.modelAndSimulation = appContext.modelAndSimulation.updateGrain(grain, new)
    }
}

@Composable
private fun FieldInteractions(appContext: AppContext, grain: Grain) {
    if (appContext.model.fields.isNotEmpty()) {
        TitleRow(appContext.locale.i18n("Field Interactions"))

        Row {
            Column(Modifier.weight(.15f)) { }
            Column(Modifier.weight(.28f)) {
                Text(
                    appContext.locale.i18n("Productions"),
                    Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Column(Modifier.weight(.28f)) {
                Text(
                    appContext.locale.i18n("Influences"),
                    Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Column(Modifier.weight(.28f)) {
                Text(
                    appContext.locale.i18n("Permeability"),
                    Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        appContext.model.fields.forEach { field ->
            Row {
                Column(Modifier.weight(.15f).align(Alignment.CenterVertically)) { Text(field.name) }
                Column(Modifier.weight(.28f).align(Alignment.CenterVertically)) {
                    fieldInteraction(appContext, grain.fieldProductions[field.id] ?: 0f) {
                        val value = if (abs(it) < .1f) 0f else it
                        val updated = grain.fieldProductions.toMutableMap().apply { this[field.id] = value }
                        val new = grain.copy(fieldProductions = updated)
                        appContext.modelAndSimulation = appContext.modelAndSimulation.updateGrain(grain, new)
                    }
                }
                Column(Modifier.weight(.28f).align(Alignment.CenterVertically)) {
                    fieldInteraction(appContext, grain.fieldInfluences[field.id] ?: 0f) {
                        val value = if (abs(it) < .1f) 0f else it
                        val updated = grain.fieldInfluences.toMutableMap().apply { this[field.id] = value }
                        val new = grain.copy(fieldInfluences = updated)
                        appContext.modelAndSimulation = appContext.modelAndSimulation.updateGrain(grain, new)
                    }
                }
                Column(Modifier.weight(.28f).align(Alignment.CenterVertically)) {
                    fieldInteraction(appContext, grain.fieldPermeable[field.id] ?: 0f, 0f.rangeTo(1f)) {
                        val updated = grain.fieldPermeable.toMutableMap().apply { this[field.id] = it }
                        val new = grain.copy(fieldPermeable = updated)
                        appContext.modelAndSimulation = appContext.modelAndSimulation.updateGrain(grain, new)
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
            value < 0f -> AppTheme.sliderNegative()
            value > 0f -> AppTheme.sliderPositive()
            else -> AppTheme.sliderNeutral()
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
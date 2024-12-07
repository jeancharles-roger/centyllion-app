package com.centyllion.ui.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centyllion.model.Field
import com.centyllion.model.colorNameList
import com.centyllion.ui.*

@Composable
fun FieldEdit(app: AppContext, field: Field) {
    Properties(app) {
        Column {
            MainTitleRow(app.locale.i18n("Field"))

            SingleLineTextEditRow(app, field, "Name", field.name) {
                app.modelAndSimulation = app.modelAndSimulation.updateField(field, field.copy(name = it))
            }

            MultiLineTextEditRow(app, field, "Description", field.description) {
                app.modelAndSimulation = app.modelAndSimulation.updateField(field, field.copy(description = it))
            }

            IntEditRow(app, field, "Half-life", field.halfLife) {
                app.modelAndSimulation = app.modelAndSimulation.updateField(field, field.copy(halfLife = it))
            }

            FloatEditRow(
                app, field, "Speed", field.speed,
                trailingRatio = .35f,
                trailingContent = {
                    Directions(app, field.allowedDirection) {
                        app.modelAndSimulation = app.modelAndSimulation.updateField(
                            field, field.copy(allowedDirection = it)
                        )
                    }
                },
            ) {
                app.modelAndSimulation = app.modelAndSimulation.updateField(field, field.copy(speed = it))
            }

            TitleRow(app.locale.i18n("Display"))

            ComboRow(
                app, field, "Color", field.color,
                colorNameList, valueContent = {
                    ColoredSquare(it)
                    Spacer(Modifier.width(4.dp))
                    Text(it)
                }, lazy = true
            ) {
                val new = field.copy(color = it)
                app.modelAndSimulation = app.modelAndSimulation.updateField(field, new)
            }

            CheckRow(app, field, "Invisible", field.invisible) {
                val new = field.copy(invisible = it)
                app.modelAndSimulation = app.modelAndSimulation.updateField(field, new)
            }
        }
    }
}

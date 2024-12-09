package com.centyllion.ui.tabs

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.centyllion.model.Field
import com.centyllion.model.colorNameList
import com.centyllion.ui.*

@Composable
fun FieldEdit(app: AppContext, field: Field) {
    Properties(app, "Field") {

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
                Directions(field.allowedDirection, @Composable { ColoredSquare(field.color) }) {
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
                Text(text = it, fontSize = 14.sp)
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

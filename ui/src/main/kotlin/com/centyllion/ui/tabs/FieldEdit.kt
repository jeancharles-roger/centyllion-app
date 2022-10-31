package com.centyllion.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centyllion.model.Field
import com.centyllion.model.colorNameList
import com.centyllion.ui.*

@Composable
fun FieldEdit(appContext: AppContext, field: Field) {
    Surface(appContext.theme.surfaceModifier) {
        Box(modifier = Modifier.padding(4.dp)) {
            Column {
                MainTitleRow(appContext.locale.i18n("Field"))

                SingleLineTextEditRow(appContext, field, "Name", field.name) {
                    appContext.model = appContext.model.updateField(field, field.copy(name = it))
                }

                MultiLineTextEditRow(appContext, field, "Description", field.description) {
                    appContext.model = appContext.model.updateField(field, field.copy(description = it))
                }

                IntEditRow(appContext, field, "Half-life", field.halfLife) {
                    appContext.model = appContext.model.updateField(field, field.copy(halfLife = it))
                }

                FloatEditRow(appContext, field, "Speed", field.speed,
                    trailingIcon = {
                        Row {
                            Directions(appContext, field.allowedDirection) {
                                appContext.model = appContext.model.updateField(
                                    field, field.copy(allowedDirection = it)
                                )
                            }
                        }
                    }
                ) {
                    appContext.model = appContext.model.updateField(field, field.copy(speed = it))
                }

                TitleRow(appContext.locale.i18n("Display"))

                ComboRow(appContext, field, "Color", field.color,
                    colorNameList, valueContent = {
                        ColoredSquare(it)
                        Spacer(Modifier.width(4.dp))
                        Text(it)
                    }, lazy = true
                ) {
                    val new = field.copy(color = it)
                    appContext.model = appContext.model.updateField(field, new)
                }

                CheckRow(appContext, field, "Invisible", field.invisible) {
                    val new = field.copy(invisible = it)
                    appContext.model = appContext.model.updateField(field, new)
                }
            }
        }
    }
}
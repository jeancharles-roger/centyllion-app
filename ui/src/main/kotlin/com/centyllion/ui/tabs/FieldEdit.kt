package com.centyllion.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.centyllion.model.Field
import com.centyllion.model.colorNameList
import com.centyllion.model.extendedDirections
import com.centyllion.model.firstDirections
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
                            firstDirections.forEachIndexed { index, direction ->
                                val selected = field.allowedDirection.contains(direction)
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
                                                if (selected) field.allowedDirection - direction else field.allowedDirection + direction
                                            appContext.model = appContext.model.updateField(
                                                field,
                                                field.copy(allowedDirection = directions)
                                            )
                                        },
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            extendedDirections.forEachIndexed { index, direction ->
                                val selected = field.allowedDirection.contains(direction)
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
                                                if (selected) field.allowedDirection - direction else field.allowedDirection + direction
                                            appContext.model = appContext.model.updateField(
                                                field,
                                                field.copy(allowedDirection = directions)
                                            )
                                        },
                                )
                            }

                            Spacer(Modifier.width(12.dp))
                        }
                    }
                ) {
                    appContext.model = appContext.model.updateField(field, field.copy(speed = it))
                }

                TitleRow(appContext.locale.i18n("Display"))

                ComboRow(appContext, field, "Color", field.color,
                    colorNameList, {
                        ColoredSquare(it)
                        Spacer(Modifier.width(4.dp))
                        Text(it)
                    }, lazy = true
                ) {
                    val new = field.copy(color = it)
                    appContext.model = appContext.model.updateField(field, new)
                }

                CustomRow(appContext, emptyList()) {
                    Text(appContext.locale.i18n("Invisible"), Modifier.align(Alignment.CenterVertically))
                    Checkbox(
                        checked = field.invisible,
                        colors = appContext.theme.checkboxColors(),
                        onCheckedChange = {
                            val new = field.copy(invisible = it)
                            appContext.model = appContext.model.updateField(field, new)
                        }
                    )
                }

            }
        }
    }
}
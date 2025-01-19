package com.centyllion.ui

import androidx.compose.material.icons.twotone.Info
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.centyllion.model.*
import compose.icons.AllIcons
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile

suspend fun openFileDialog(
    title: String,
    allowedExtensions: List<String>? = null,
): PlatformFile? = FileKit.pickFile(
    type = PickerType.File(allowedExtensions),
    mode = PickerMode.Single,
    title = title,
)

fun ModelElement.icon(): ImageVector = when (this) {
    is GrainModel -> FontAwesomeIcons.Solid.Boxes
    is Field -> FontAwesomeIcons.Solid.Podcast
    is Grain -> FontAwesomeIcons.Solid.SquareFull
    is Behaviour -> FontAwesomeIcons.Solid.ExchangeAlt
    else -> FontAwesomeIcons.Solid.Question
}

fun Severity.icon(): ImageVector = when (this) {
    Severity.Info -> androidx.compose.material.icons.Icons.TwoTone.Info
    Severity.Warning -> androidx.compose.material.icons.Icons.TwoTone.Warning
    Severity.Severe -> androidx.compose.material.icons.Icons.TwoTone.Warning
}

fun Direction.icon(): ImageVector = when (this) {
    com.centyllion.model.Direction.Left -> FontAwesomeIcons.Solid.ArrowLeft
    com.centyllion.model.Direction.Right -> FontAwesomeIcons.Solid.ArrowRight
    com.centyllion.model.Direction.Up -> FontAwesomeIcons.Solid.ArrowUp
    com.centyllion.model.Direction.Down -> FontAwesomeIcons.Solid.ArrowDown
    com.centyllion.model.Direction.LeftUp -> FontAwesomeIcons.Solid.Question
    com.centyllion.model.Direction.LeftDown -> FontAwesomeIcons.Solid.Question
    com.centyllion.model.Direction.RightUp -> FontAwesomeIcons.Solid.Question
    com.centyllion.model.Direction.RightDown -> FontAwesomeIcons.Solid.Question
}


val allIcons = buildMap {
    put("square", FontAwesomeIcons.Solid.Square)
    put("squarefull", FontAwesomeIcons.Solid.SquareFull)
    put("circle", FontAwesomeIcons.Solid.Circle)
    FontAwesomeIcons.AllIcons
        .sortedBy { it.name }
        .forEach { put(it.name.lowercase(), it) }
}

fun Double.toFixedString(decimals: Int = 2): String = String.format("%.${decimals}f", this)

fun Float.toFixedString(decimals: Int = 2): String = toDouble().toFixedString(decimals)

val Triple<Int, Int, Int>.color get() = Color(first, second, third)

fun Triple<Int, Int, Int>.alphaColor(alpha: Int) = Color(first, second, third, alpha)
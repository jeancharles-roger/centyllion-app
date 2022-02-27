package com.centyllion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.model.*
import compose.icons.AllIcons
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneScope
import java.awt.Cursor
import java.awt.FileDialog
import java.io.File

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSplitPaneApi::class)
fun SplitPaneScope.horizontalSplitter() {
    splitter {
        visiblePart {
            Box(
                Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colors.background)
            )
        }
        handle {
            Box(
                Modifier
                    .markAsHandle()
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                    .background(SolidColor(Color.Gray), alpha = 0.50f)
                    .width(10.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSplitPaneApi::class)
fun SplitPaneScope.verticalSplitter() {
    splitter {
        visiblePart {
            Box(
                Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
            )
        }
        handle {
            Box(
                Modifier
                    .markAsHandle()
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))
                    .background(SolidColor(Color.Gray), alpha = 0.50f)
                    .height(10.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun RowScope.Label(text: String) {
    Text(
        text = text, fontSize = 12.sp,
        modifier = Modifier.padding(6.dp).align(Alignment.CenterVertically)
    )
}

fun openFileDialog(
    window: ComposeWindow, title: String,
    allowedExtensions: List<String>, allowMultiSelection: Boolean = false
): Set<File> {
    return FileDialog(window, title, FileDialog.LOAD).apply {
        isMultipleMode = allowMultiSelection

        // windows
        file = allowedExtensions.joinToString(";") { "*$it" } // e.g. '*.jpg'

        // linux
        setFilenameFilter { _, name ->
            allowedExtensions.any {
                name.endsWith(it)
            }
        }

        isVisible = true
    }.files.toSet()
}

fun newFileDialog(
    window: ComposeWindow, title: String,
    preselectedFile: String? = null,
    allowedExtensions: List<String> = emptyList(),
    allowMultiSelection: Boolean = false
): Set<File> {
    return FileDialog(window, title, FileDialog.SAVE).apply {
        isMultipleMode = allowMultiSelection

        // windows
        preselectedFile?.let { file = it }

        // linux
        setFilenameFilter { _, name ->
            allowedExtensions.any { name.endsWith(it) }
        }

        isVisible = true
    }.files.toSet()
}

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

fun com.centyllion.model.Direction.icon(): ImageVector = when (this) {
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
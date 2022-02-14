package com.centyllion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.lightColors
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.centyllion.i18n.Locale
import com.centyllion.model.GrainModel
import com.centyllion.model.ModelElement
import com.centyllion.model.Simulation
import com.centyllion.ui.tabs.Tab
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.skia.Font
import java.nio.file.Path

interface AppContext {
    val locale: Locale

    val theme: AppTheme

    val window: ComposeWindow
    val scope: CoroutineScope

    val centerTabs: List<Tab>
    var centerSelectedTab: Tab
    val southTabs: List<Tab>
    var southSelectedTab: Tab

    val path: Path?
    var model: GrainModel
    var simulation: Simulation
    var selection: List<ModelElement>

    fun showPrimarySelection()

    // TODO use diagnostic from components
    //val diagnostics : List<Diagnostic>
    //var selectedDiagnostic: Diagnostic?

    val logs: List<AppLog>

    fun log(message: String)
    fun warn(message: String)
    fun alert(message: String)
}

enum class Severity {
    Info, Warning, Severe
}

class AppLog(
    val timestamp: Long,
    val message: String,
    val severity: Severity
)

val themeColors: Colors = lightColors(
    primary = Color(73, 131, 227),
    primaryVariant = Color(73, 131, 227),
)

class AppTheme(
    val colors: Colors = themeColors,
    val warning: Color = Color(237, 162, 0)
) {
    val iconPadding get() = 8.dp

    val toolbarIconSize get() = 24.dp + iconPadding

    val toolbarSpacerModifier get() = Modifier
        .padding(horizontal = 4.dp)
        .background(Color.Gray.copy(alpha = .7f))
        .width(2.dp)
        .height(toolbarIconSize)

    val toolBarIconModifier get() = Modifier
        .size(toolbarIconSize)
        .padding(iconPadding/2)
        .background(color = Color(0xAAEEEEEE), shape = RoundedCornerShape(4.dp))
        .padding(iconPadding/2)

    val buttonIconModifier get() = Modifier
        .size(20.dp)
        .padding(horizontal = 4.dp)

    val surfaceModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp, horizontal = 8.dp)
        .background(
            color = colors.background,
            shape = RoundedCornerShape(corner = CornerSize(8.dp))
        )
        .padding(8.dp)

    fun severityColor(severity: Severity) = when (severity) {
        Severity.Info -> Color.Black
        Severity.Warning -> warning
        Severity.Severe -> colors.error
    }

    val componentFont = Font().also {
        it.size = 16f
    }
    val providedFont = Font()

    val componentRadius get() = 3f

}
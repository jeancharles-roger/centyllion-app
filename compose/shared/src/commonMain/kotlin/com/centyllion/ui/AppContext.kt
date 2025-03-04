package com.centyllion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Colors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.centyllion.i18n.Locale
import com.centyllion.model.*
import com.centyllion.ui.tabs.Tab
import io.github.vinceglb.filekit.core.PlatformFile
import org.jetbrains.skia.Font

interface AppContext {
    val locale: Locale

    val centerTabs: List<Tab>
    var centerSelectedTab: Tab

    val eastTabs: List<Tab>
    var eastSelectedTab: Tab

    val path: PlatformFile?
    var modelAndSimulation: ModelAndSimulation
    var model: GrainModel
    var simulation: Simulation

    var expertMode: Boolean

    var running: Boolean
    val step: Int
    var simulator: Simulator
    var selection: List<ModelElement>

    fun showPrimarySelection()

    val problems : List<Problem>
    var selectedProblem: Problem?

    val logs: List<AppLog>

    fun log(message: String)
    fun warn(message: String)
    fun alert(message: String)
}

enum class Severity {
    Info, Warning, Severe
}

class AppLog(
    val message: String,
    val severity: Severity
)

val themeColors: Colors = lightColors(
    primary = Color(0x95, 0x95, 0xf8),
    primaryVariant = Color(73, 131, 227),
    background = Color(221, 221, 235),
    surface = Color(240, 240, 240),
)

object AppTheme {

    val colors: Colors = themeColors
    val warning: Color = Color(237, 162, 0)
    val backgroundMedium: Color = Color(
        red = colors.background.red - .2f,
        green = colors.background.green - .2f,
        blue = colors.background.blue - .2f,
    )

    @Composable
    fun checkboxColors() = CheckboxDefaults.colors(colors.primary)

    @Composable
    fun sliderNeutral() = SliderDefaults.colors(
        thumbColor = Color.Gray,
        activeTrackColor = Color.LightGray,
    )

    @Composable
    fun sliderPositive() = SliderDefaults.colors(
        thumbColor = colors.primary,
        activeTrackColor = colors.primary,
    )

    @Composable
    fun sliderNegative() = SliderDefaults.colors(
        thumbColor = Color.Yellow,
        activeTrackColor = Color.Yellow,
    )

    val iconPadding get() = 8.dp

    val toolbarIconSize get() = 24.dp + iconPadding

    val toolbarSpacerModifier get() = Modifier
        .padding(horizontal = 4.dp)
        .background(Color.Gray.copy(alpha = .7f))
        .width(2.dp)
        .height(toolbarIconSize)

    val toolBarIconModifier get() = Modifier
        .size(toolbarIconSize)
        .padding(iconPadding)

    val buttonIconModifier get() = Modifier
        .size(28.dp)
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
package com.centyllion.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centyllion.ui.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Cogs

object EnvironmentTab : Tab {
    override val nameKey = "Environment"
    override val icon = FontAwesomeIcons.Solid.Cogs

    @Composable
    override fun content(app: AppContext) {
        Surface(app.theme.surfaceModifier) {
            Box(modifier = Modifier.padding(4.dp)) {
                Column {
                    MainTitleRow(app.locale.i18n("Model"))

                    SingleLineTextEditRow(app, app.model, "Name", app.model.name) {
                        app.modelAndSimulation = app.modelAndSimulation.updateInfo(name = it)
                    }

                    MultiLineTextEditRow(app, app.model, "Description", app.model.description) {
                        app.modelAndSimulation = app.modelAndSimulation.updateInfo(description = it)
                    }

                    TitleRow(app.locale.i18n("Simulation"))


                }
            }
        }
    }
}
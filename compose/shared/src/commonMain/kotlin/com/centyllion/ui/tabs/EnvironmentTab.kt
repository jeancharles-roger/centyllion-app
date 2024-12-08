package com.centyllion.ui.tabs

import androidx.compose.runtime.Composable
import com.centyllion.ui.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Cogs

object EnvironmentTab : Tab {
    override val nameKey = "Environment"
    override val icon = FontAwesomeIcons.Solid.Cogs

    @Composable
    override fun content(app: AppContext) {
        Properties(app, "Model") {
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
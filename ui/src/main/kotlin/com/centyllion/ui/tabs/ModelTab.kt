package com.centyllion.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.centyllion.ui.AppContext
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ProjectDiagram

object ModelTab : Tab {
    override val name = "Model"
    override val icon = FontAwesomeIcons.Solid.ProjectDiagram

    @Composable
    override fun content(appContext: AppContext) {
        Box {

        }
    }
}


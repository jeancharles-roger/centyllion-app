package com.centyllion.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centyllion.model.GrainModel
import com.centyllion.ui.AppContext
import com.centyllion.ui.MainTitleRow
import com.centyllion.ui.MultiLineTextEditRow
import com.centyllion.ui.SingleLineTextEditRow

@Composable
fun GrainModelEdit(appContext: AppContext, model: GrainModel) {
    Surface(appContext.theme.surfaceModifier) {
        Box(modifier = Modifier.padding(4.dp)) {
            Column {
                MainTitleRow(appContext.locale.i18n("Model"))

                SingleLineTextEditRow(appContext, model, "Name", model.name) {
                    appContext.model = appContext.model.copy(name = it)
                }

                MultiLineTextEditRow(appContext, model, "Description", model.description) {
                    appContext.model = appContext.model.copy(description = it)
                }

            }
        }
    }
}
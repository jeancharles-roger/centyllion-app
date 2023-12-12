package com.centyllion.ui.dialog

import androidx.compose.runtime.Composable
import com.centyllion.ui.AppContext

interface Dialog {
    val titleKey: String
    @Composable fun content(appContext: AppContext)
}
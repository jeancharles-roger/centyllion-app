package com.centyllion.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.centyllion.model.Behaviour
import com.centyllion.ui.*

@Composable
fun BehaviourEdit(appContext: AppContext, behaviour: Behaviour) {
    Surface(appContext.theme.surfaceModifier) {
        Box(modifier = Modifier.padding(4.dp)) {
            Column {
                MainTitleRow(appContext.locale.i18n("Behaviour"))

                SingleLineTextEditRow(appContext, behaviour, "Name", behaviour.name) {
                    appContext.model = appContext.model.updateBehaviour(behaviour, behaviour.copy(name = it))
                }

                MultiLineTextEditRow(appContext, behaviour, "Description", behaviour.description) {
                    appContext.model = appContext.model.updateBehaviour(behaviour, behaviour.copy(description = it))
                }

                CustomRow(appContext, problems(appContext, behaviour, "Speed")) {
                    Text(appContext.locale.i18n("Speed"), Modifier.align(Alignment.CenterVertically))
                    Label(behaviour.probability.toFixedString(3))
                    Slider(
                        value = behaviour.probability.toFloat(),
                        valueRange = 0f.rangeTo(1f),
                        steps = 100,
                        onValueChange = {
                            val new = behaviour.copy(probability = it.toDouble())
                            appContext.model = appContext.model.updateBehaviour(behaviour, new)
                        }
                    )
                }

            }
        }
    }
}

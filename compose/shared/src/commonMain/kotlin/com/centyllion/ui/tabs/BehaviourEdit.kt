package com.centyllion.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.model.Behaviour
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.Reaction
import com.centyllion.ui.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Plus

@Composable
fun BehaviourEdit(appContext: AppContext, behaviour: Behaviour) {
    Properties(appContext, "Behaviour") {
        SingleLineTextEditRow(appContext, behaviour, "Name", behaviour.name) {
            appContext.modelAndSimulation =
                appContext.modelAndSimulation.updateBehaviour(behaviour, behaviour.copy(name = it))
        }

        MultiLineTextEditRow(appContext, behaviour, "Description", behaviour.description) {
            appContext.modelAndSimulation =
                appContext.modelAndSimulation.updateBehaviour(behaviour, behaviour.copy(description = it))
        }

        propertyRow(appContext, behaviour, "Speed") {
            Text(behaviour.probability.toFixedString(3), Modifier.align(Alignment.CenterVertically))
            Slider(
                value = behaviour.probability.toFloat(),
                valueRange = 0f.rangeTo(1f),
                onValueChange = {
                    val new = behaviour.copy(probability = it.toDouble())
                    appContext.modelAndSimulation = appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                }
            )
        }


        IntPredicateRow(appContext, behaviour, behaviour.agePredicate, "When age") {
            val new = behaviour.copy(agePredicate = it)
            appContext.modelAndSimulation = appContext.modelAndSimulation.updateBehaviour(behaviour, new)
        }

        MainTitleRow(appContext.locale.i18n("Reactions"))

        Row {
            Text(appContext.locale.i18n("Reactives"), Modifier.weight(.24f), fontSize = 12.sp)
            Text(appContext.locale.i18n("Directions"), Modifier.weight(.24f), fontSize = 12.sp)
            Text(appContext.locale.i18n("Products"), Modifier.weight(.24f), fontSize = 12.sp)
            Text(appContext.locale.i18n("Sources"), Modifier.weight(.24f), fontSize = 12.sp)
        }

        propertyRow(appContext, behaviour, "") {
            val mainReactive = appContext.model.grainForId(behaviour.mainReactiveId)
            GrainCombo(mainReactive, appContext.model, Modifier.weight(.24f)) {
                if (it != null) {
                    val new = behaviour.copy(mainReactiveId = it.id)
                    appContext.modelAndSimulation = appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                }
            }

            Text("", Modifier.weight(.24f), fontSize = 12.sp)

            val mainProduct = appContext.model.grainForId(behaviour.mainProductId)
            GrainCombo(mainProduct, appContext.model, Modifier.weight(.24f)) {
                if (it != null) {
                    val new = behaviour.copy(mainProductId = it.id)
                    appContext.modelAndSimulation = appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                }
            }

            Text("", Modifier.weight(.24f), fontSize = 12.sp)

            IconButton(
                modifier = Modifier.defaultMinSize(16.dp, 16.dp).weight(0.04f),
                onClick = {
                    val reaction = Reaction()
                    val new = behaviour.copy(reaction = behaviour.reaction + reaction)
                    appContext.modelAndSimulation = appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                }
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Plus,
                    contentDescription = "Add Reaction",
                    modifier = AppTheme.buttonIconModifier
                )
            }
        }

        behaviour.reaction.forEach { reaction ->
            row {
                val mainReactive = appContext.model.grainForId(reaction.reactiveId)
                GrainCombo(mainReactive, appContext.model, Modifier.weight(.24f)) {
                    val new = behaviour.updateReaction(reaction, reaction.copy(reactiveId = it?.id ?: -1))
                    appContext.modelAndSimulation = appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                }

                Row(modifier = Modifier.weight(.24f)) {
                    Directions(reaction.allowedDirection, @Composable { GrainSquare(mainReactive) }) {
                        val newReaction = reaction.copy(allowedDirection = it)
                        val new = behaviour.updateReaction(reaction, newReaction)
                        appContext.modelAndSimulation =
                            appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                    }
                }

                Text("", Modifier.weight(.24f), fontSize = 12.sp)

                val mainProduct = appContext.model.grainForId(reaction.productId)
                GrainCombo(mainProduct, appContext.model, Modifier.weight(.24f)) {
                    val new = behaviour.updateReaction(reaction, reaction.copy(productId = it?.id ?: -1))
                    appContext.modelAndSimulation = appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                }

                Text("", Modifier.weight(.24f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RowScope.GrainCombo(
    grain: Grain?, model: GrainModel,
    modifier: Modifier = Modifier,
    onValueChange: (Grain?) -> Unit
) {
    Combo(
        selected = grain, values = model.grains, modifier = modifier,
        valueContent = {
            if (it != null) {
                ColoredGrain(it)
                Spacer(Modifier.width(2.dp))
                Text(it.name)
            } else {
                EmptyGrain()
                Spacer(Modifier.width(2.dp))
                Text("none")
            }
        },
        onValueChange = onValueChange
    )
}
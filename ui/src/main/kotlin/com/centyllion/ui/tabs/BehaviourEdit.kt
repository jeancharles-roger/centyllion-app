package com.centyllion.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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

                IntPredicateRow(appContext, behaviour, behaviour.agePredicate, "When age") {
                    val new = behaviour.copy(agePredicate = it)
                    appContext.model = appContext.model.updateBehaviour(behaviour, new)
                }

                MainTitleRow(appContext.locale.i18n("Reactions"))

                Row {
                    Text(appContext.locale.i18n("Reactives"), Modifier.weight(.24f), fontSize = 12.sp)
                    Text(appContext.locale.i18n("Directions"), Modifier.weight(.24f), fontSize = 12.sp)
                    Text(appContext.locale.i18n("Products"), Modifier.weight(.24f), fontSize = 12.sp)
                    Text(appContext.locale.i18n("Sources"), Modifier.weight(.24f), fontSize = 12.sp)
                }

                CustomRow(appContext, emptyList()) {
                    val mainReactive = appContext.model.grainForId(behaviour.mainReactiveId)
                    GrainCombo(mainReactive, appContext.model, Modifier.weight(.24f)) {
                        if (it != null) {
                            val new = behaviour.copy(mainReactiveId = it.id)
                            appContext.model = appContext.model.updateBehaviour(behaviour, new)
                        }
                    }

                    Text("", Modifier.weight(.24f), fontSize = 12.sp)

                    val mainProduct = appContext.model.grainForId(behaviour.mainProductId)
                    GrainCombo(mainProduct, appContext.model, Modifier.weight(.24f)) {
                        if (it != null) {
                            val new = behaviour.copy(mainProductId = it.id)
                            appContext.model = appContext.model.updateBehaviour(behaviour, new)
                        }
                    }

                    Text("", Modifier.weight(.24f), fontSize = 12.sp)

                    IconButton(
                        modifier = Modifier.defaultMinSize(16.dp, 16.dp).weight(0.04f),
                        onClick =  {
                            val reaction = Reaction()
                            val new = behaviour.copy(reaction = behaviour.reaction + reaction)
                            appContext.model = appContext.model.updateBehaviour(behaviour, new)
                        }
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Plus,
                            contentDescription = "Add Reaction",
                            modifier = appContext.theme.buttonIconModifier
                        )
                    }
                }

                behaviour.reaction.forEach { reaction ->
                    CustomRow(appContext, emptyList()) {
                        val mainReactive = appContext.model.grainForId(reaction.reactiveId)
                        GrainCombo(mainReactive, appContext.model, Modifier.weight(.24f)) {
                            val new = behaviour.updateReaction(reaction, reaction.copy(reactiveId = it?.id ?: -1))
                            appContext.model = appContext.model.updateBehaviour(behaviour, new)
                        }


                        Row(modifier = Modifier.weight(.24f)) {
                            Directions(appContext, reaction.allowedDirection, size = 18.dp) {
                                val newReaction = reaction.copy(allowedDirection = it)
                                val new = behaviour.updateReaction(reaction, newReaction)
                                appContext.model = appContext.model.updateBehaviour(behaviour, new)
                            }
                        }

                        Text("", Modifier.weight(.24f), fontSize = 12.sp)

                        val mainProduct = appContext.model.grainForId(reaction.productId)
                        GrainCombo(mainProduct, appContext.model, Modifier.weight(.24f)) {
                            val new = behaviour.updateReaction(reaction, reaction.copy(productId = it?.id ?: -1))
                            appContext.model = appContext.model.updateBehaviour(behaviour, new)
                        }

                        Text("", Modifier.weight(.24f), fontSize = 12.sp)
                    }
                }
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
    val content: @Composable (Grain?) -> Unit = {
        if (it != null) {
            ColoredGrain(it)
            Spacer(Modifier.width(2.dp))
            Text(it.name)
        } else {
            EmptyGrain()
            Spacer(Modifier.width(2.dp))
            Text("none")
        }
    }
    Combo(grain, model.grains, modifier, content, onValueChange)
}
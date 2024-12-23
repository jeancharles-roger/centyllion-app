package com.centyllion.ui.tabs

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import compose.icons.fontawesomeicons.solid.Minus
import compose.icons.fontawesomeicons.solid.Plus
import kotlinx.coroutines.GlobalScope
import java.nio.file.Path

@Composable
@Preview
fun Preview() {
    val app = AppState(null, GlobalScope, mutableStateOf(Path.of("/Users/charlie/Downloads/phalenes1.netbiodyn")))
    //GrainItem(app, app.model.grains.first())
    //GrainEdit(app, app.model.grains.first())

    //BehaviourItem(app, app.model.behaviours.first())
    BehaviourEdit(app, app.model.behaviours.first())

    //FieldItem(app, app.model.fields.first())
}

@Composable
fun BehaviourEdit(appContext: AppContext, behaviour: Behaviour) {
    Properties(
        title = { MainTitleRow(appContext.locale.i18n("Behaviour")) }
    ) {
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

        val rows = buildList<List<@Composable (() -> Unit)>> {
            add(listOf(
                    { Text(appContext.locale.i18n("Reactives"), fontSize = 12.sp) },
                    { Text(appContext.locale.i18n("Directions"), fontSize = 12.sp) },
                    { Text(appContext.locale.i18n("Products"), fontSize = 12.sp) },
                    { Text(appContext.locale.i18n("Sources"), fontSize = 12.sp) },
                ))
            add(listOf(
                    {
                        val mainReactive = appContext.model.grainForId(behaviour.mainReactiveId)
                        GrainCombo(mainReactive, false, appContext.model) {
                            if (it != null) {
                                val new = behaviour.copy(mainReactiveId = it.id)
                                appContext.modelAndSimulation =
                                    appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                            }
                        }
                    },
                    { },
                    {
                        val mainProduct = appContext.model.grainForId(behaviour.mainProductId)
                        GrainCombo(mainProduct, true, appContext.model) {
                            if (it != null) {
                                val new = behaviour.copy(mainProductId = it.id)
                                appContext.modelAndSimulation =
                                    appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                            }
                        }
                    },
                    { },
                    {
                        IconButton(
                            modifier = Modifier.defaultMinSize(16.dp, 16.dp),
                            onClick = {
                                val reaction = Reaction()
                                val new = behaviour.copy(reaction = behaviour.reaction + reaction)
                                appContext.modelAndSimulation =
                                    appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                            }
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Plus,
                                contentDescription = "Add Reaction",
                                modifier = AppTheme.buttonIconModifier
                            )
                        }
                    }
                ))
            behaviour.reaction.forEach { reaction ->
                add(
                    listOf(
                        {
                            val mainReactive = appContext.model.grainForId(reaction.reactiveId)
                            GrainCombo(mainReactive, true, appContext.model) {
                                val new =
                                    behaviour.updateReaction(reaction, reaction.copy(reactiveId = it?.id ?: -1))
                                appContext.modelAndSimulation =
                                    appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                            }
                        },
                        {
                            val mainReactive = appContext.model.grainForId(reaction.reactiveId)
                            if (mainReactive != null) {
                                Directions(reaction.allowedDirection, @Composable { GrainSquare(mainReactive) }) {
                                    val newReaction = reaction.copy(allowedDirection = it)
                                    val new = behaviour.updateReaction(reaction, newReaction)
                                    appContext.modelAndSimulation =
                                        appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                                }
                            }
                        },
                        {
                            val mainProduct = appContext.model.grainForId(reaction.productId)
                            GrainCombo(mainProduct, true, appContext.model) {
                                val new =
                                    behaviour.updateReaction(reaction, reaction.copy(productId = it?.id ?: -1))
                                appContext.modelAndSimulation =
                                    appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                            }
                        },
                        { },

                        {
                            IconButton(
                                modifier = Modifier.defaultMinSize(16.dp, 16.dp),
                                onClick = {
                                    val new = behaviour.copy(reaction = behaviour.reaction - reaction)
                                    appContext.modelAndSimulation =
                                        appContext.modelAndSimulation.updateBehaviour(behaviour, new)
                                }
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Minus,
                                    contentDescription = "Remove Reaction",
                                    modifier = AppTheme.buttonIconModifier
                                )
                            }
                        }
                    )
                )
            }
        }

        GridProperties(
            columns = listOf(.24f, .24f, .24f, .24f, .04f),
            rows
        )
    }
}

@Composable
fun GridProperties(columns: List<Float>, rows: List<List<@Composable () -> Unit>>) {
    Column {
        rows.forEach { row ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                columns.forEachIndexed { index, column ->
                    val cell = row.getOrNull(index)
                    if (cell != null) Box(
                        modifier = Modifier.weight(column),
                        contentAlignment = Alignment.Center
                    ) { cell() }
                }
            }
        }
    }
}

@Composable
fun GrainCombo(
    grain: Grain?, canBeNone: Boolean, model: GrainModel,
    modifier: Modifier = Modifier,
    onValueChange: (Grain?) -> Unit
) {
    val values = if (canBeNone) listOf(null) + model.grains else model.grains
    Combo(
        selected = grain, values = values, modifier = modifier,
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
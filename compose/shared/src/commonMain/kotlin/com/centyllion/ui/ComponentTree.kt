package com.centyllion.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.model.Behaviour
import com.centyllion.model.Field
import com.centyllion.model.Grain
import com.centyllion.model.colorNames
import com.centyllion.ui.tabs.SimulationTab
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun ComponentTree(modifier: Modifier, appState: AppState) {
    Box(modifier = modifier) {
        val listState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier.padding(6.dp).background(AppTheme.colors.background),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item {
                SectionItem(appState, FontAwesomeIcons.Solid.Square, "Grains") {
                    val grain = appState.model.newGrain(appState.locale.i18n("Grain"))
                    appState.modelAndSimulation = appState.modelAndSimulation.addGrain(grain)
                }
            }
            items(appState.modelAndSimulation.model.grains) {
                Box(Modifier.padding(6.dp)) { GrainItem(appState, it) }
            }

            item {
                SectionItem(appState, FontAwesomeIcons.Solid.ExchangeAlt, "Behaviours") {
                    val behaviour = appState.model.newBehaviour(appState.locale.i18n("Behaviour"))
                    appState.modelAndSimulation = appState.modelAndSimulation.addBehaviour(behaviour)
                }
            }
            items(appState.modelAndSimulation.model.behaviours) {
                Box(Modifier.padding(6.dp)) { BehaviourItem(appState, it) }
            }

            item {
                SectionItem(appState, FontAwesomeIcons.Solid.Podcast, "Fields") {
                    val field = appState.model.newField(appState.locale.i18n("Field"))
                    appState.modelAndSimulation = appState.modelAndSimulation.addField(field)
                }
            }

            items(appState.modelAndSimulation.model.fields) {
                Box(Modifier.padding(6.dp)) { FieldItem(appState, it) }
            }

        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = listState)
        )
    }
}

@Composable
fun SectionItem(
    appState: AppState, icon: ImageVector, titleKey: String,
    addCallback: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .background(AppTheme.colors.surface)
            .padding(6.dp)
    ) {
        Icon(
            imageVector = icon, contentDescription = null,
            modifier = Modifier.height(22.dp).align(Alignment.CenterVertically),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            color = AppTheme.colors.onSurface,
            text = AnnotatedString(appState.locale.i18n(titleKey)),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.weight(1F).align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(8.dp))

        NbdButton(
            FontAwesomeIcons.Solid.Plus,
            "Add $titleKey",
            addCallback
        )

        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun FieldItem(
    appState: AppState,
    field: Field,
) {
    val selected = appState.selection.contains(field)
    val color = when {
        selected -> AppTheme.colors.onPrimary
        else -> AppTheme.colors.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (selected) AppTheme.colors.primary else AppTheme.colors.surface,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(6.dp)
            .clickable { appState.selection = listOf(field) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        ColoredSquare(field.color)


        val text = buildAnnotatedString {
            append(field.name)
            if (appState.centerSelectedTab == SimulationTab) {
                val count = appState.fieldAmounts[field.id]?.toFixedString(1) ?: "0.0"
                append(" ($count)")
            }
        }
        Text(
            color = color, text = text,
            fontSize = 14.sp, overflow = TextOverflow.Ellipsis,
        )

        Row(
            modifier = Modifier.height(16.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            if (field.invisible) Icon(
                modifier = Modifier.height(IntrinsicSize.Max),
                imageVector = FontAwesomeIcons.Solid.EyeSlash,
                contentDescription = "invisible",
                tint = color.copy(alpha = 0.5f),
            )
            if (field.speed > 0f) Icon(
                modifier = Modifier.height(IntrinsicSize.Max),
                imageVector = FontAwesomeIcons.Solid.Walking,
                contentDescription = "canMove",
                tint = color.copy(alpha = 0.5f),
            )
            if (field.halfLife > 0) Icon(
                modifier = Modifier.height(IntrinsicSize.Max),
                imageVector = FontAwesomeIcons.Solid.SkullCrossbones,
                contentDescription = "dies",
                tint = color.copy(alpha = 0.5f),
            )
        }


        NbdSmallRoundButton(
            FontAwesomeIcons.Solid.Times,
            "Remove ${field.name}",
            { appState.modelAndSimulation = appState.modelAndSimulation.dropField(field) }
        )

    }
}

@Composable
fun GrainItem(appState: AppState, grain: Grain, ) {

    val selected = appState.selection.contains(grain)
    val color = when {
        selected -> AppTheme.colors.onPrimary
        else -> AppTheme.colors.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (selected) AppTheme.colors.primary else AppTheme.colors.surface,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(6.dp)
            .clickable { appState.selection = listOf(grain) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GrainSquare(grain)

        val text = buildAnnotatedString {
            append(grain.name)
            if (appState.centerSelectedTab == SimulationTab) {
                val count = appState.grainCounts[grain.id]?.toString() ?: "0"
                append(" ($count)")
            }
        }
        Text(
            color = color, text = text,
            fontSize = 14.sp, overflow = TextOverflow.Ellipsis,
        )

        Row(
            modifier = Modifier.height(16.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            if (grain.invisible) Icon(
                modifier = Modifier.height(IntrinsicSize.Max),
                imageVector = FontAwesomeIcons.Solid.EyeSlash,
                contentDescription = "invisible",
                tint = color.copy(alpha = 0.5f),
            )
            if (grain.canMove) Icon(
                modifier = Modifier.height(IntrinsicSize.Max),
                imageVector = FontAwesomeIcons.Solid.Walking,
                contentDescription = "canMove",
                tint = color.copy(alpha = 0.5f),
            )
            if (grain.halfLife > 0) Icon(
                modifier = Modifier.height(IntrinsicSize.Max),
                imageVector = FontAwesomeIcons.Solid.SkullCrossbones,
                contentDescription = "dies",
                tint = color.copy(alpha = 0.5f),
            )
        }

        NbdSmallRoundButton(
            FontAwesomeIcons.Solid.Times,
            "Remove ${grain.name}",
            { appState.modelAndSimulation = appState.modelAndSimulation.dropGrain(grain) }
        )
    }
}

@Composable
fun BehaviourItem(
    appState: AppState,
    behaviour: Behaviour,
) {
    val selected = appState.selection.contains(behaviour)
    val color = when {
        selected -> AppTheme.colors.onPrimary
        else -> AppTheme.colors.onSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (selected) AppTheme.colors.primary else AppTheme.colors.surface,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(6.dp)
            .clickable { appState.selection = listOf(behaviour) }
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                color = color,
                text = AnnotatedString(behaviour.name),
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.weight(1F).align(Alignment.CenterVertically)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier =
                        Modifier
                            .background(Color.Green, shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 5.dp),
                    text = behaviour.probability.toFixedString(3),
                    fontSize = 14.sp,
                )

                NbdSmallRoundButton(
                    FontAwesomeIcons.Solid.Times,
                    "Remove ${behaviour.name}",
                    { appState.modelAndSimulation = appState.modelAndSimulation.dropBehaviour(behaviour) }
                )
            }
        }

        Row(Modifier.fillMaxWidth()) {
            GrainSquareRow(appState, behaviour.reactiveGrainIds)

            Icon(
                imageVector = FontAwesomeIcons.Solid.ArrowRight,
                contentDescription = null,
                modifier = Modifier.height(20.dp).align(Alignment.CenterVertically),
            )

            GrainSquareRow(appState, behaviour.productGrainIds)
        }

    }
}

@Composable
fun RowScope.GrainSquareRow(appState: AppState, ids: List<Int>) {
    Row(
        modifier = Modifier.weight(.5f).padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        var first = true

        ids.map { appState.modelAndSimulation.model.grainForId(it) }
            .forEach {
                if (!first) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Plus,
                        contentDescription = null,
                        modifier = Modifier.height(10.dp).align(Alignment.CenterVertically),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                GrainSquare(it)
                first = false
            }
    }
}

@Composable
fun GrainSquare(grain: Grain?) = if (grain != null) ColoredGrain(grain) else EmptyGrain()

@Composable
fun ColoredGrain(grain: Grain) {
    val color = colorNames[grain.color]
        ?.let { Color(it.first, it.second, it.third) }
        ?: Color.Red

    val icon = allIcons[grain.iconName] ?: FontAwesomeIcons.Solid.SquareFull

    Icon(
        imageVector = icon,
        contentDescription = null, tint = color,
        modifier = Modifier.height(18.dp),
    )
}

@Composable
fun EmptyGrain() {
    Icon(
        imageVector = FontAwesomeIcons.Solid.TimesCircle,
        contentDescription = null,
        modifier = Modifier.height(18.dp),
    )
}

@Composable
fun ColoredSquare(color: String) {
    val squareColor = colorNames[color]
        ?.let { Color(it.first, it.second, it.third) }
        ?: Color.Red

    Icon(
        imageVector = FontAwesomeIcons.Solid.SquareFull,
        contentDescription = null, tint = squareColor,
        modifier = Modifier.height(18.dp),
    )
}


@Composable
fun NbdButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.size(30.dp),
        onClick = onClick,
        contentPadding = PaddingValues(5.dp),
        shape = RoundedCornerShape(10.dp),
    ) {
        Icon(
            modifier = Modifier.size(25.dp),
            imageVector = icon,
            contentDescription = text,
        )
    }
}

@Composable
fun NbdSmallRoundButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.size(20.dp),
        onClick = onClick,
        contentPadding = PaddingValues(5.dp),
        shape = RoundedCornerShape(10.dp),
    ) {
        Icon(
            modifier = Modifier.size(25.dp),
            imageVector = icon,
            contentDescription = text,
        )
    }
}
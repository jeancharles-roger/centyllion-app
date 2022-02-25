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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.model.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.AllIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*

@Composable
fun ComponentTree(appState: AppState) {
    Box(Modifier.padding(4.dp)) {
        val listState = rememberLazyListState()

        LazyColumn(state = listState) {
            item { TreeItem(appState, appState.model) }

            item {
                SectionItem(appState, FontAwesomeIcons.Solid.Podcast, "Fields") {
                    val field = appState.model.newField(appState.locale.i18n("Field"))
                    appState.model = appState.model.copy(fields = appState.model.fields + field)
                }
            }
            items(appState.model.fields) { FieldItem(appState, it) }

            item {
                SectionItem(appState, FontAwesomeIcons.Solid.Square, "Grains") {
                    val grain = appState.model.newGrain(appState.locale.i18n("Grain"))
                    appState.model = appState.model.copy(grains = appState.model.grains + grain)
                }
            }
            items(appState.model.grains) { GrainItem(appState, it) }

            item {
                SectionItem(appState, FontAwesomeIcons.Solid.ExchangeAlt, "Behaviours") {
                    val behaviour = appState.model.newBehaviour(appState.locale.i18n("Behaviour"))
                    appState.model = appState.model.copy(behaviours = appState.model.behaviours + behaviour)
                }
            }
            items(appState.model.behaviours) { BehaviourItem(appState, it) }
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
            .background(appState.theme.colors.surface)
            .padding(6.dp)
    ) {
        Icon(
            imageVector = icon, contentDescription = null,
            modifier = Modifier.height(22.dp).align(Alignment.CenterVertically),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            color = appState.theme.colors.onSurface,
            text = AnnotatedString(appState.locale.i18n(titleKey)),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.weight(1F).align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            //modifier = appState.theme.buttonIconModifier,
            onClick = addCallback
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.Plus,
                contentDescription = "Add $titleKey",
                modifier = appState.theme.buttonIconModifier
            )
        }

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
        selected -> appState.theme.colors.onPrimary
        else -> appState.theme.colors.onSurface
    }

    Row(
        modifier = Modifier
            .background(if (selected) appState.theme.colors.primary else appState.theme.colors.surface)
            .clickable { appState.selection = listOf(field) }
            .padding(6.dp)
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        ColoredSquare(field.color)
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            color = color,
            text = AnnotatedString(field.name),
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.weight(1F).align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = FontAwesomeIcons.Solid.TimesCircle,
            contentDescription = null, tint = Color.LightGray,
            modifier = Modifier
                .height(14.dp).align(Alignment.CenterVertically)
                .clickable { appState.model = appState.model.dropField(field) }
        )
    }
}

@Composable
fun GrainItem(
    appState: AppState,
    grain: Grain,
) {
    val selected = appState.selection.contains(grain)
    val color = when {
        selected -> appState.theme.colors.onPrimary
        else -> appState.theme.colors.onSurface
    }

    Row(
        modifier = Modifier
            .background(if (selected) appState.theme.colors.primary else appState.theme.colors.surface)
            .clickable { appState.selection = listOf(grain) }
            .padding(6.dp)
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        GrainSquare(grain)
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            color = color,
            text = AnnotatedString(grain.name),
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.weight(1F).align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = FontAwesomeIcons.Solid.TimesCircle,
            contentDescription = null, tint = Color.LightGray,
            modifier = Modifier
                .height(14.dp).align(Alignment.CenterVertically)
                .clickable { appState.model = appState.model.dropGrain(grain) }
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
        selected -> appState.theme.colors.onPrimary
        else -> appState.theme.colors.onSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) appState.theme.colors.primary else appState.theme.colors.surface)
            .clickable { appState.selection = listOf(behaviour) }
            .padding(6.dp)
    ) {

        Row {
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                color = color,
                text = AnnotatedString(behaviour.name),
                fontSize = 12.sp,
                maxLines = 1,
                modifier = Modifier.weight(1F).align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = behaviour.probability.toFixedString(3),
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(5.dp)
                    .background(Color.Green, shape = RoundedCornerShape(16.dp))
                    .padding(3.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = FontAwesomeIcons.Solid.TimesCircle,
                contentDescription = null, tint = Color.LightGray,
                modifier = Modifier
                    .height(14.dp).align(Alignment.CenterVertically)
                    .clickable { appState.model = appState.model.dropBehaviour(behaviour) }
            )
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

        ids.map { appState.model.grainForId(it) }
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
fun RowScope.GrainSquare(grain: Grain?) = if (grain != null) ColoredGrain(grain) else EmptyGrain()

@Composable
fun RowScope.ColoredGrain(grain: Grain) {
    val color = colorNames[grain.color]
        ?.let { Color(it.first, it.second, it.third) }
        ?: Color.Red

    val iconName = grain.iconName
    val icon = FontAwesomeIcons.Solid.AllIcons.find { it.name.equals(iconName, true)}
        ?: FontAwesomeIcons.Solid.SquareFull

    Icon(
        imageVector = icon,
        contentDescription = null, tint = color,
        modifier = Modifier.height(20.dp).align(Alignment.CenterVertically),
    )
}

@Composable
fun RowScope.EmptyGrain() {
    Icon(
        imageVector = FontAwesomeIcons.Solid.TimesCircle,
        contentDescription = null,
        modifier = Modifier.height(16.dp).align(Alignment.CenterVertically),
    )
}

@Composable
fun RowScope.ColoredSquare(color: String) {
    val squareColor = colorNames[color]
        ?.let { Color(it.first, it.second, it.third) }
        ?: Color.Red

    Icon(
        imageVector = FontAwesomeIcons.Solid.SquareFull,
        contentDescription = null, tint = squareColor,
        modifier = Modifier.height(20.dp).align(Alignment.CenterVertically),
    )
}


@Composable
fun TreeItem(
    appState: AppState,
    element: ModelElement,
) {
    val selected = appState.selection.contains(element)
    val colorWithError = when {
        selected -> appState.theme.colors.onPrimary
        else -> appState.theme.colors.onSurface
    }

    val color = when {
        selected -> appState.theme.colors.onPrimary
        else -> appState.theme.colors.onSurface
    }

    Row(
        modifier = Modifier
            .background(if (selected) appState.theme.colors.primary else appState.theme.colors.surface)
            .clickable { appState.selection = listOf(element) }
            .padding(6.dp)
    ) {
        Icon(
            imageVector = element.icon(), contentDescription = null,
            tint = colorWithError,
            modifier = Modifier.height(20.dp).align(Alignment.CenterVertically),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            color = color,
            text = AnnotatedString(element.name),
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.weight(1F).align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(8.dp))
    }

}

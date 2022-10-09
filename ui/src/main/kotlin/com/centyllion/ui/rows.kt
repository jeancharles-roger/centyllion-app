package com.centyllion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.model.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.AngleDown

@Composable
fun MainTitleRow(title: String, extension: @Composable RowScope.() -> Unit = {}, ) {
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.W800,
            textDecoration = TextDecoration.Underline,
            fontSize = 20.sp,
            modifier = Modifier.weight(1f)
        )
        extension()
    }
}


@Composable
fun TitleRow(title: String, extension: @Composable RowScope.() -> Unit = {},) {
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.W800,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        extension()
    }
}

@Composable
fun SingleLineTextEditRow(
    appContext: AppContext, element: ModelElement,
    property: String, value: String,
    validationProperty: String = property,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
) = TextEditRow(appContext, element, property, value, validationProperty, 1, trailingIcon, onValueChange)

@Composable
fun MultiLineTextEditRow(
    appContext: AppContext, element: ModelElement,
    property: String, value: String,
    validationProperty: String = property,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
) = TextEditRow(appContext, element, property, value, validationProperty, 10, trailingIcon, onValueChange)

@Composable
fun TextEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: String,
    validationProperty: String = property,
    maxLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
) {
    val problems = problems(appContext, element, validationProperty)
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {
        TextField(
            label = { Text(appContext.locale.i18n(property)) },
            value = value,
            trailingIcon = trailingIcon,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            isError = problems.isNotEmpty(),
            singleLine = maxLines <= 1,
            maxLines = maxLines
        )
    }
    problems.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun IntEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: Int,
    validationProperty: String = property,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (Int) -> Unit,
) {
    val problems = problems(appContext, element, validationProperty)
    CustomRow(appContext, element, property) {
        IntEdit(appContext, property, value, problems.isNotEmpty(), trailingIcon, onValueChange)
    }
    problems.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun IntEdit(
    appContext: AppContext, property: String,
    value: Int, hasProblem: Boolean,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (Int) -> Unit,
) {
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value.toString())) }
    val newValue = if (textFieldValueState.text.toIntOrNull() != value) value.toString() else textFieldValueState.text
    val textFieldValue = textFieldValueState.copy(text = newValue)

    TextField(
        label = if (property.isBlank()) null else { { Text(appContext.locale.i18n(property)) } },
        value = textFieldValue,
        trailingIcon = trailingIcon,
        onValueChange = {
            textFieldValueState = it
            val new = it.text.toIntOrNull()
            if (new != null && value != new) {
                onValueChange(new)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        isError = textFieldValue.text.toIntOrNull() == null || hasProblem,
        singleLine = true,
    )
}

@Composable
fun DoubleEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: Double,
    validationProperty: String = property,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (Double) -> Unit,
) {
    val problems = problems(appContext, element, validationProperty)
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {

        var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value.toString())) }
        val newValue = if (textFieldValueState.text.toDoubleOrNull() != value) value.toString() else textFieldValueState.text
        val textFieldValue = textFieldValueState.copy(text = newValue)

        TextField(
            label = { Text(appContext.locale.i18n(property)) },
            value = textFieldValue,
            trailingIcon = trailingIcon,
            onValueChange = {
                textFieldValueState = it
                val new = it.text.toDoubleOrNull()
                if (new != null && value != new) {
                    onValueChange(new)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = textFieldValue.text.toDoubleOrNull() == null || problems.isNotEmpty(),
            singleLine = true,
        )
    }
    problems.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun FloatEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: Float,
    validationProperty: String = property,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (Float) -> Unit,
) {
    val problems = problems(appContext, element, validationProperty)
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {

        var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value.toString())) }
        val newValue = if (textFieldValueState.text.toFloatOrNull() != value) value.toString() else textFieldValueState.text
        val textFieldValue = textFieldValueState.copy(text = newValue)

        TextField(
            label = { Text(appContext.locale.i18n(property)) },
            value = textFieldValue,
            trailingIcon = trailingIcon,
            onValueChange = {
                textFieldValueState = it
                val new = it.text.toFloatOrNull()
                if (new != null && value != new) {
                    onValueChange(new)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = textFieldValue.text.toFloatOrNull() == null || problems.isNotEmpty(),
            singleLine = true,
        )
    }
    problems.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun CheckRow(
    appContext: AppContext, element: ModelElement,
    property: String, checked: Boolean,
    extension: @Composable RowScope.() -> Unit = {},
    onCheckedChange: (Boolean) -> Unit,
) {
    val diagnostics = problems(appContext, element, property)
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {
        Text(property, modifier = Modifier.align(Alignment.CenterVertically))
        Checkbox(
            checked = checked,
            modifier = Modifier.align(Alignment.CenterVertically),
            onCheckedChange = onCheckedChange
        )
        extension()
    }
    diagnostics.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun ComboRow(
    appContext: AppContext, element: ModelElement,
    property: String, selected: String, values: List<String>,
    valueContent: @Composable (String) -> Unit = { Text(it) },
    lazy: Boolean = false,
    extension: @Composable RowScope.() -> Unit = {},
    onValueChange: (String) -> Unit,
) {
    val diagnostics = problems(appContext, element, property)
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {
        Text(appContext.locale.i18n(property), modifier = Modifier.align(Alignment.CenterVertically))
        Spacer(Modifier.width(20.dp))
        if (lazy) LazyCombo(selected, values, Modifier, valueContent, onValueChange)
        else Combo(selected, values, Modifier, valueContent, onValueChange)
        extension()
    }
    diagnostics.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun <T> RowScope.LazyCombo(
    selected: T, values: List<T>,
    modifier: Modifier = Modifier,
    valueContent: @Composable (T) -> Unit,
    onValueChange: (T) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    valueContent(selected)
    Icon(
        FontAwesomeIcons.Solid.AngleDown,
        "Expand",
        modifier = modifier
            .size(20.dp).align(Alignment.CenterVertically)
            .clickable { expanded.value = !expanded.value }
    )

    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.size(300.dp, 300.dp)
        ) {
            items(values) { value ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(value)
                        expanded.value = false
                    }
                ) { valueContent(value) }
            }
        }
    }
}

@Composable
fun <T> RowScope.Combo(
    selected: T, values: List<T>,
    modifier: Modifier = Modifier,
    valueContent: @Composable (T) -> Unit,
    onValueChange: (T) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    Row(modifier.fillMaxWidth()) { valueContent(selected) }
    Icon(
        FontAwesomeIcons.Solid.AngleDown,
        "Expand",
        modifier = modifier
            .height(20.dp).align(Alignment.CenterVertically)
            .clickable { expanded.value = !expanded.value }
    )

    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        for (value in values) {
            DropdownMenuItem(
                onClick = {
                    onValueChange(value)
                    expanded.value = false
                }
            ) { valueContent(value) }
        }
    }
}

@Composable
fun CustomRow(
    appContext: AppContext, element: ModelElement, property: String,
    content: @Composable RowScope.() -> Unit = {},
) {
    val diagnostics = problems(appContext, element, property)
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp), content = content)
    diagnostics.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun CustomRow(
    appContext: AppContext, diagnostics: List<Problem>,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp), content = content)
    diagnostics.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun IntPredicateRow(
    appContext: AppContext, element: ModelElement,
    predicate: Predicate<Int>, property: String,
    onValueChange: (Predicate<Int>) -> Unit
) {
    val diagnostics = problems(appContext, element, property)
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {
        Text(appContext.locale.i18n(property), modifier = Modifier.align(Alignment.CenterVertically))
        Spacer(Modifier.width(20.dp))
        PredicateOpCombo(predicate.op) {
            onValueChange(predicate.copy(op = it))
        }
        IntEdit(appContext, "", predicate.constant, diagnostics.isNotEmpty()) {
            onValueChange(predicate.copy(constant = it))
        }
    }
    diagnostics.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun RowScope.PredicateOpCombo(op: Operator, onValueChange: (Operator) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    Text(op.label, Modifier.align(Alignment.CenterVertically))
    Icon(
        FontAwesomeIcons.Solid.AngleDown,
        "Expand",
        modifier =
        Modifier
            .size(20.dp).align(Alignment.CenterVertically)
            .clickable { expanded.value = !expanded.value }
    )

    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.size(300.dp, 300.dp)
        ) {
            items(Operator.values()) { value ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(value)
                        expanded.value = false
                    }
                ) { Text(value.label) }
            }
        }
    }
}

@Composable
fun Directions(
    appContext: AppContext, allowedDirection: Set<Direction>,
    size: Dp = 28.dp,
    onValueChange: (Set<Direction>) -> Unit
) {

    firstDirections.forEachIndexed { index, direction ->
        val selected = allowedDirection.contains(direction)
        val shape = when (index) {
            0 -> RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
            firstDirections.size - 1 -> RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
            else -> RectangleShape
        }
        Icon(
            imageVector = direction.icon(), contentDescription = null,
            tint = if (selected) appContext.theme.colors.onPrimary else appContext.theme.colors.primary,
            modifier = Modifier.size(size)
                .background(
                    color = if (selected) appContext.theme.colors.primary else appContext.theme.colors.onPrimary,
                    shape = shape
                )
                .padding(5.dp, 2.dp)
                .clickable {
                    val directions = if (selected) allowedDirection - direction else allowedDirection + direction
                    onValueChange(directions)
                },
        )
    }

    Spacer(Modifier.width(12.dp))

    extendedDirections.forEachIndexed { index, direction ->
        val selected = allowedDirection.contains(direction)
        val shape = when (index) {
            0 -> RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
            extendedDirections.size - 1 -> RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
            else -> RectangleShape
        }
        Icon(
            imageVector = direction.icon(), contentDescription = null,
            tint = if (selected) appContext.theme.colors.onPrimary else appContext.theme.colors.primary,
            modifier = Modifier.size(size)
                .background(
                    color = if (selected) appContext.theme.colors.primary else appContext.theme.colors.onPrimary,
                    shape = shape
                )
                .padding(5.dp, 2.dp)
                .clickable {
                    val directions = if (selected) allowedDirection - direction else allowedDirection + direction
                    onValueChange(directions)
                },
        )
    }

    Spacer(Modifier.width(12.dp))
}


fun problems(appContext: AppContext, element: ModelElement, name: String) =
    appContext.problems.filter { it.source == element && it.property.equals(name, true) }

@Composable
fun ProblemItemRow(
    appContext: AppContext,
    diagnostic: Problem,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(6.dp)) {
        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            contentDescription = "Problem",
            tint = if (selected) appContext.theme.colors.primary else appContext.theme.colors.error,
            imageVector = Icons.TwoTone.Warning,
            modifier = Modifier.fillMaxHeight().align(Alignment.CenterVertically),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            color = if (selected) appContext.theme.colors.onPrimary else appContext.theme.colors.onError,
            text = AnnotatedString(diagnostic.message),
            fontSize = 12.sp,
            modifier = Modifier.weight(1F).align(Alignment.CenterVertically),
            maxLines = 1,
        )

        Spacer(modifier = Modifier.width(8.dp))
    }
}

package com.centyllion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.centyllion.model.Direction as ModelDirection

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
fun TitleRow(title: String, extension: @Composable RowScope.() -> Unit = {}) {
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
fun propertyRow(
    appContext: AppContext, element: ModelElement, property: String, validationProperty: String = property,
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    editPart: @Composable RowScope.() -> Unit,
) {
    val problems = appContext.problems.filter { it.source == element && it.property.equals(validationProperty, true) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 18.dp)) {
        Column(modifier = Modifier.fillMaxWidth(.2f).align(Alignment.CenterVertically)) {
            Text(text = appContext.locale.i18n(property), modifier = Modifier.fillMaxWidth())
        }

        Column(modifier = Modifier
            .fillMaxWidth(if (trailingIcon != null) 1f - trailingRatio else 1f)
            .align(Alignment.CenterVertically),
            content = { Row { editPart() } }
        )

        if (trailingIcon != null) {
            Column(modifier = Modifier.fillMaxWidth(trailingRatio), content = trailingIcon)
        }
    }
    problems.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun propertyRow(
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    editPart: @Composable RowScope.() -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 18.dp)) {
        if (trailingIcon != null) {
            Column(modifier = Modifier
                .fillMaxWidth(1f - trailingRatio)
                .align(Alignment.CenterVertically),
                content = { Row { editPart() } }
            )
            Column(modifier = Modifier.fillMaxWidth(trailingRatio), content = trailingIcon)
        } else {
            editPart()
        }
    }
}

@Composable
fun SingleLineTextEditRow(
    appContext: AppContext, element: ModelElement,
    property: String, value: String,
    validationProperty: String = property,
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    onValueChange: (String) -> Unit,
) = TextEditRow(appContext, element, property, value, validationProperty, 1, trailingIcon, trailingRatio, onValueChange)

@Composable
fun MultiLineTextEditRow(
    appContext: AppContext, element: ModelElement,
    property: String, value: String,
    validationProperty: String = property,
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    onValueChange: (String) -> Unit,
) = TextEditRow(appContext, element, property, value, validationProperty, 10, trailingIcon, trailingRatio, onValueChange)

@Composable
fun TextEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: String,
    validationProperty: String = property,
    maxLines: Int = 1,
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    onValueChange: (String) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingIcon, trailingRatio) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = maxLines > 1, maxLines = maxLines,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = appContext.theme.colors.background, shape = RoundedCornerShape(3.dp))
            .padding(4.dp)
    )
}

@Composable
fun IntEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: Int,
    validationProperty: String = property,
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    onValueChange: (Int) -> Unit,
) = propertyRow(
    appContext = appContext,
    element = element,
    property = property,
    validationProperty = validationProperty,
    trailingIcon = trailingIcon,
    trailingRatio = trailingRatio
) {
    IntTextField(appContext, value, onValueChange)
}

@Composable
fun DoubleEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: Double,
    validationProperty: String = property,
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    onValueChange: (Double) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingIcon, trailingRatio) {
    DoubleTextField(appContext, value, onValueChange)
}

@Composable
fun FloatEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: Float,
    validationProperty: String = property,
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    onValueChange: (Float) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingIcon, trailingRatio) {
    FloatTextField(appContext, value, onValueChange)
}

@Composable
fun CheckRow(
    appContext: AppContext, element: ModelElement,
    property: String, checked: Boolean,
    validationProperty: String = property,
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    onCheckedChange: (Boolean) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingIcon, trailingRatio) {
    Checkbox(checked = checked, onCheckedChange = onCheckedChange)
}

@Composable
fun ComboRow(
    appContext: AppContext, element: ModelElement,
    property: String, selected: String, values: List<String>,
    validationProperty: String = property,
    valueContent: @Composable (String) -> Unit = { Text(it) },
    lazy: Boolean = false,
    trailingIcon: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .5f,
    onValueChange: (String) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingIcon, trailingRatio) {
    if (lazy) LazyCombo(selected, values, Modifier, valueContent, onValueChange)
    else Combo(selected, values, Modifier, valueContent, onValueChange)
}

@Composable
fun <T> LazyCombo(
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
            .size(20.dp)
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
    //Column(
    //    Modifier.align(Alignment.CenterVertically).clickable { expanded.value = !expanded.value }
    //) {
        Row(modifier.fillMaxWidth()) { valueContent(selected) }
        Icon(
            FontAwesomeIcons.Solid.AngleDown, "Expand",
            modifier = modifier
                .height(20.dp)
                .clickable { expanded.value = !expanded.value }
        )
    //}
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
fun IntPredicateRow(
    appContext: AppContext, element: ModelElement,
    predicate: Predicate<Int>, property: String,
    onValueChange: (Predicate<Int>) -> Unit
) = propertyRow(appContext, element, property) {
    Column(Modifier.fillMaxWidth(.2f)) {
        PredicateOpCombo(predicate.op) {
            onValueChange(predicate.copy(op = it))
        }
    }
    IntTextField(appContext, predicate.constant) {
        onValueChange(predicate.copy(constant = it))
    }
}

@Composable
fun PredicateOpCombo(op: Operator, onValueChange: (Operator) -> Unit) {
    Row {
        val expanded = remember { mutableStateOf(false) }
        Text(op.label)
        Icon(
            FontAwesomeIcons.Solid.AngleDown,
            "Expand",
            modifier =
            Modifier
                .size(20.dp)
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
}

@Composable
fun IntTextField(
    appContext: AppContext,
    value: Int,
    onValueChange: (Int) -> Unit,
) = GenericTextField(
    appContext = appContext,
    value = value,
    toValue = String::toIntOrNull,
    toString = Int::toString,
    onValueChange = onValueChange
)

@Composable
fun FloatTextField(
    appContext: AppContext,
    value: Float,
    onValueChange: (Float) -> Unit,
) = GenericTextField(
    appContext = appContext,
    value = value,
    toValue = String::toFloatOrNull,
    toString = Float::toString,
    onValueChange = onValueChange
)
@Composable
fun DoubleTextField(
    appContext: AppContext,
    value: Double,
    onValueChange: (Double) -> Unit,
) = GenericTextField(
    appContext = appContext,
    value = value,
    toValue = String::toDoubleOrNull,
    toString = Double::toString,
    onValueChange = onValueChange
)

@Composable
fun <T> GenericTextField(
    appContext: AppContext,
    value: T,
    toValue: String.() -> T?,
    toString: T.() -> String,
    onValueChange: (T) -> Unit,
) {
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value.toString())) }
    val newValue = if (textFieldValueState.text.toValue() != value) value.toString() else textFieldValueState.text
    val textFieldValue = textFieldValueState.copy(text = newValue)

    BasicTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValueState = it
            val new = it.text.toValue()
            if (new != null && value != new) {
                onValueChange(new)
            }
        },
        modifier = Modifier
            .fillMaxWidth(1f)
            .background(color = appContext.theme.colors.background, shape = RoundedCornerShape(3.dp))
            .padding(4.dp)
    )
}

@Composable
fun Directions(
    appContext: AppContext, allowedDirection: Set<ModelDirection>,
    size: Dp = 28.dp, onValueChange: (Set<ModelDirection>) -> Unit
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

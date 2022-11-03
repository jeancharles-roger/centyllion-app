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
import com.centyllion.model.ModelElement
import com.centyllion.model.Operator
import com.centyllion.model.Predicate
import com.centyllion.model.Problem
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
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    editPart: @Composable RowScope.() -> Unit,
) {
    val problems = appContext.problems.filter { it.source == element && it.property.equals(validationProperty, true) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 18.dp)) {
        Column(modifier = Modifier.weight(.2f).align(Alignment.CenterVertically)) {
            Text(text = appContext.locale.i18n(property), modifier = Modifier.fillMaxWidth())
        }

        Column(
            modifier = Modifier
                .weight(if (trailingContent != null) .8f - trailingRatio else .8f)
                .align(Alignment.CenterVertically),
            content = { Row { editPart() } }
        )

        if (trailingContent != null) {
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(trailingRatio).align(Alignment.CenterVertically),
                content = trailingContent
            )
        }
    }
    problems.forEach { ProblemItemRow(appContext, it) }
}

@Composable
fun row(
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    editPart: @Composable RowScope.() -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 18.dp)) {
        if (trailingContent != null) {
            Column(modifier = Modifier
                .fillMaxWidth(1f - trailingRatio)
                .align(Alignment.CenterVertically),
                content = { Row { editPart() } }
            )
            Column(modifier = Modifier.fillMaxWidth(trailingRatio), content = trailingContent)
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
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    onValueChange: (String) -> Unit,
) = TextEditRow(appContext, element, property, value, validationProperty, 1, trailingContent, trailingRatio, onValueChange)

@Composable
fun MultiLineTextEditRow(
    appContext: AppContext, element: ModelElement,
    property: String, value: String,
    validationProperty: String = property,
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    onValueChange: (String) -> Unit,
) = TextEditRow(appContext, element, property, value, validationProperty, 10, trailingContent, trailingRatio, onValueChange)

@Composable
fun TextEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: String,
    validationProperty: String = property,
    maxLines: Int = 1,
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    onValueChange: (String) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingContent, trailingRatio) {
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
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    onValueChange: (Int) -> Unit,
) = propertyRow(
    appContext = appContext,
    element = element,
    property = property,
    validationProperty = validationProperty,
    trailingContent = trailingContent,
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
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    onValueChange: (Double) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingContent, trailingRatio) {
    DoubleTextField(appContext, value, onValueChange)
}

@Composable
fun FloatEditRow(
    appContext: AppContext, element: ModelElement,
    property: String,
    value: Float,
    validationProperty: String = property,
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    onValueChange: (Float) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingContent, trailingRatio) {
    FloatTextField(appContext, value, onValueChange)
}

@Composable
fun CheckRow(
    appContext: AppContext, element: ModelElement,
    property: String, checked: Boolean,
    validationProperty: String = property,
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    onCheckedChange: (Boolean) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingContent, trailingRatio) {
    Checkbox(checked = checked, onCheckedChange = onCheckedChange)
}

@Composable
fun ComboRow(
    appContext: AppContext, element: ModelElement,
    property: String, selected: String, values: List<String>,
    validationProperty: String = property,
    valueContent: @Composable (String) -> Unit = { Text(it) },
    lazy: Boolean = false,
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    onValueChange: (String) -> Unit,
) = propertyRow(appContext, element, property, validationProperty, trailingContent, trailingRatio) {
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
fun SimpleTextField(
    appContext: AppContext,
    value: String,
    onValueChange: (String) -> Unit,
) = GenericTextField(
    appContext = appContext,
    value = value,
    toValue = { this },
    toString = { this },
    onValueChange = onValueChange
)
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
    size: Dp = 20.dp, onValueChange: (Set<ModelDirection>) -> Unit
) {
    Row {
        ModelDirection.first.forEachIndexed { index, direction ->
            val selected = allowedDirection.contains(direction)
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                ModelDirection.first.size - 1 -> RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                else -> RectangleShape
            }
            Icon(
                imageVector = direction.icon(), contentDescription = null,
                tint = if (selected) appContext.theme.colors.onPrimary else appContext.theme.colors.primary,
                modifier = Modifier.size(size)
                    .align(Alignment.CenterVertically)
                    .background(
                        color = if (selected) appContext.theme.colors.primary else appContext.theme.colors.onPrimary,
                        shape = shape
                    )
                    .padding(5.dp, 2.dp)
                    .clickable {
                        val directions = if (selected) allowedDirection - direction else allowedDirection + direction
                        onValueChange(directions)
                    }
            )
        }

        Spacer(Modifier.width(12.dp))

        ModelDirection.extended.forEachIndexed { index, direction ->
            val selected = allowedDirection.contains(direction)
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                ModelDirection.extended.size - 1 -> RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                else -> RectangleShape
            }
            Icon(
                imageVector = direction.icon(), contentDescription = null,
                tint = if (selected) appContext.theme.colors.onPrimary else appContext.theme.colors.primary,
                modifier = Modifier.size(size)
                    .align(Alignment.CenterVertically)
                    .background(
                        color = if (selected) appContext.theme.colors.primary else appContext.theme.colors.onPrimary,
                        shape = shape
                    )
                   .padding(5.dp, 2.dp)
                    .clickable {
                        val directions = if (selected) allowedDirection - direction else allowedDirection + direction
                        onValueChange(directions)
                    }
            )
        }
    }
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

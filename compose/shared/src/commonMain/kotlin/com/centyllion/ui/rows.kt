package com.centyllion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
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
fun Properties(title: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Surface(AppTheme.surfaceModifier) {
        Column(modifier = Modifier.padding(4.dp)) {
            title()
            content()
        }
    }
}


@Composable
fun MainTitleRow(
    title: String,
    prefix: @Composable RowScope.() -> Unit = {},
    suffix: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        prefix()
        Text(
            text = title,
            fontWeight = FontWeight.W800,
            textDecoration = TextDecoration.Underline,
            fontSize = 20.sp,
            modifier = Modifier.weight(1f)
        )
        suffix()
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
    appContext: AppContext, element: ModelElement,
    property: String, validationProperty: String = property,
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    editPart: @Composable RowScope.() -> Unit,
) {
    val problems = appContext.problems.filter { it.source == element && it.property.equals(validationProperty, true) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.width(120.dp),
            text = appContext.locale.i18n(property),
        )

        editPart()

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
            Column(
                modifier = Modifier
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
) = TextEditRow(
    appContext = appContext,
    element = element,
    property = property,
    value = value,
    validationProperty = validationProperty,
    maxLines = 1,
    trailingContent = trailingContent,
    trailingRatio = trailingRatio,
    onValueChange = onValueChange
)

@Composable
fun MultiLineTextEditRow(
    appContext: AppContext, element: ModelElement,
    property: String, value: String,
    validationProperty: String = property,
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingRatio: Float = .3f,
    onValueChange: (String) -> Unit,
) = TextEditRow(
    appContext = appContext,
    element = element,
    property = property,
    value = value,
    validationProperty = validationProperty,
    maxLines = 10,
    trailingContent = trailingContent,
    trailingRatio = trailingRatio,
    onValueChange = onValueChange
)

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
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.colors.background, shape = RoundedCornerShape(3.dp))
            .padding(4.dp),
        value = value,
        onValueChange = onValueChange,
        singleLine = maxLines > 1, maxLines = maxLines,
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
    IntTextField(value, onValueChange)
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
    DoubleTextField(value, onValueChange)
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
    FloatTextField(value, onValueChange)
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

    Row(
        modifier = Modifier.background(
            color = AppTheme.colors.background,
            shape = RoundedCornerShape(15.dp)
            )
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .clickable { expanded.value = !expanded.value },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        // selected element
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) { valueContent(selected) }

        Icon(
            FontAwesomeIcons.Solid.AngleDown,
            "Expand",
            modifier = modifier.size(20.dp)
        )

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            val lazyListState = rememberLazyListState(values.indexOf(selected))
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
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) { valueContent(value) }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> Combo(
    selected: T, values: List<T>,
    modifier: Modifier = Modifier,
    valueContent: @Composable (T) -> Unit,
    onValueChange: (T) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.background(
            color = AppTheme.colors.background,
            shape = RoundedCornerShape(15.dp)
            )
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .clickable { expanded.value = !expanded.value },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        valueContent(selected)
        Icon(
            FontAwesomeIcons.Solid.AngleDown, "Expand",
            modifier = modifier.height(20.dp)
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
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) { valueContent(value) }
                }
            }
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
    IntTextField(
        value = predicate.constant,
        onValueChange = { onValueChange(predicate.copy(constant = it)) }
    )
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
                items(Operator.entries) { value ->
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
    value: String,
    onValueChange: (String) -> Unit,
) = GenericTextField(
    value = value,
    toValue = { this },
    toString = { this },
    onValueChange = onValueChange
)

@Composable
fun IntTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
) = GenericTextField(
    value = value,
    toValue = String::toIntOrNull,
    toString = Int::toString,
    onValueChange = onValueChange
)

@Composable
fun FloatTextField(
    value: Float,
    onValueChange: (Float) -> Unit,
) = GenericTextField(
    value = value,
    toValue = String::toFloatOrNull,
    toString = Float::toString,
    onValueChange = onValueChange
)

@Composable
fun DoubleTextField(
    value: Double,
    onValueChange: (Double) -> Unit,
) = GenericTextField(
    value = value,
    toValue = String::toDoubleOrNull,
    toString = Double::toString,
    onValueChange = onValueChange
)

@Composable
fun <T> GenericTextField(
    value: T,
    toValue: String.() -> T?,
    toString: T.() -> String,
    modifier: Modifier = Modifier,
    onValueChange: (T) -> Unit,
) {
    var textFieldValueState by remember(value) {
        mutableStateOf(TextFieldValue(text = toString(value)))
    }
    val new = textFieldValueState.text.toValue()
    val stroke = if (new != null) Color.Unspecified else Color.Red

    BasicTextField(
        value = textFieldValueState,
        onValueChange = {
            textFieldValueState = it
            if (new != null && value != new) onValueChange(new)
        },
        singleLine = true,
        textStyle = TextStyle(color = stroke),
        modifier = modifier
            .background(
                color = AppTheme.colors.background,
                shape = RoundedCornerShape(3.dp)
            )
            .padding(4.dp)
    )
}

@Composable
fun DirectionCell(
    direction: ModelDirection?,
    allowedDirection: Set<ModelDirection>,
    active: @Composable () -> Unit,
    onValueChange: (Set<ModelDirection>) -> Unit
) {
    val modifier = if (direction != null) Modifier
        .border(2.dp, AppTheme.colors.background)
        .clickable {
            val new = if (allowedDirection.contains(direction)) allowedDirection - direction
            else allowedDirection + direction
            onValueChange(new)
        }
    else Modifier
        .background(Color.DarkGray)

    Box(modifier.size(20.dp)) {
        if (allowedDirection.contains(direction)) active()
    }
}

@Composable
fun Directions(
    allowedDirection: Set<ModelDirection>,
    active: @Composable () -> Unit,
    onValueChange: (Set<ModelDirection>) -> Unit
) {
    Column {
        Row {
            DirectionCell(ModelDirection.LeftUp, allowedDirection, active, onValueChange)
            DirectionCell(ModelDirection.Up, allowedDirection, active, onValueChange)
            DirectionCell(ModelDirection.RightUp, allowedDirection, active, onValueChange)
        }
        Row {
            DirectionCell(ModelDirection.Left, allowedDirection, active, onValueChange)
            DirectionCell(null, allowedDirection, active, onValueChange)
            DirectionCell(ModelDirection.Right, allowedDirection, active, onValueChange)
        }
        Row {
            DirectionCell(ModelDirection.LeftDown, allowedDirection, active, onValueChange)
            DirectionCell(ModelDirection.Down, allowedDirection, active, onValueChange)
            DirectionCell(ModelDirection.RightDown, allowedDirection, active, onValueChange)
        }
    }
}

@Composable
fun ProblemItemRow(
    app: AppContext,
    diagnostic: Problem,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(6.dp)) {
        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            contentDescription = "Problem",
            tint = if (selected) AppTheme.colors.primary else AppTheme.colors.error,
            imageVector = Icons.TwoTone.Warning,
            modifier = Modifier.fillMaxHeight().align(Alignment.CenterVertically),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            color = if (selected) AppTheme.colors.onPrimary else AppTheme.colors.error,
            text = AnnotatedString(diagnostic.message),
            fontSize = 12.sp,
            modifier = Modifier.weight(1F).align(Alignment.CenterVertically),
            maxLines = 1,
        )

        Spacer(modifier = Modifier.width(8.dp))
    }
}

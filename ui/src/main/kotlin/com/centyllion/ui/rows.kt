package com.centyllion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centyllion.model.ModelElement
import com.centyllion.model.Problem
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
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {

        var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value.toString())) }
        val newValue = if (textFieldValueState.text.toIntOrNull() != value) value.toString() else textFieldValueState.text
        val textFieldValue = textFieldValueState.copy(text = newValue)

        TextField(
            label = { Text(appContext.locale.i18n(property)) },
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
            isError = textFieldValue.text.toIntOrNull() == null || problems.isNotEmpty(),
            singleLine = true,
        )
    }
    problems.forEach { ProblemItemRow(appContext, it) }
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
    extension: @Composable RowScope.() -> Unit = {},
    onValueChange: (String) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    val diagnostics = problems(appContext, element, property)
    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {
        Text(appContext.locale.i18n(property), modifier = Modifier.align(Alignment.CenterVertically))
        Spacer(Modifier.width(20.dp))
        valueContent(selected)
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
        extension()
    }
    diagnostics.forEach { ProblemItemRow(appContext, it) }
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
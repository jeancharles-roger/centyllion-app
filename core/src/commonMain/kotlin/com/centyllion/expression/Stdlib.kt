package com.centyllion.expression

import kotlin.math.*

/**
 * Registers `pi` and `e` constant.
 */
fun MutableMap<String, Value>.registerConstants() {
    put("pi", PI.toFloat().v)
    put("e", E.toFloat().v)
}

fun defaultConstants() = buildMap { registerConstants() }

fun MutableMap<String, (List<Value>) -> Value>.unaryFunction(name: String, block: (Value) -> Value) {
    put(name) {
        require(it.size == 1) { "Function '$name' only accept one parameter" }
        block(it.first())
    }
}

fun MutableMap<String, (List<Value>) -> Value>.function(name: String, block: (List<Value>) -> Value) {
    put(name, block)
}

/**
 * Registers math functions:
 *
 * - `abs(v)` for absolute value
 * - `cos(v)`, `sin(v)`, `tan(v)`, `acos(v)`, `asin(v)`, `atan(v)` for trigonometry
 * - `floor(v)`, `ceil(v)` and `round(v)` to round doubles
 * - `sum(v...)`, `avg(v...)`, `min(v...)`, `max(v...)` for sum, average, min and max
 * - `ln(x)`, `log(x, base)` for logs
 *
 */
fun MutableMap<String, (List<Value>) -> Value>.registerFunctions() {
    unaryFunction("abs") { it.abs() }

    unaryFunction("cos") { cos(it.float).v }
    unaryFunction("sin") { sin(it.float).v }
    unaryFunction("tan") { tan(it.float).v }
    unaryFunction("acos") { acos(it.float).v }
    unaryFunction("asin") { asin(it.float).v }
    unaryFunction("atan") { atan(it.float).v }

    unaryFunction("floor") { if (it is IntegerValue) it else floor(it.float).roundToInt().v }
    unaryFunction("ceil") { if (it is IntegerValue) it else ceil(it.float).roundToInt().v }
    unaryFunction("round") { if (it is IntegerValue) it else round(it.float).roundToInt().v }

    //function("avg") { values -> (values.sumOf { it.float }/values.size).v }
    //function("sum") { values -> (values.sumOf { it.float }).v }
    function("min") { values ->
        if (values.none { it !is IntegerValue }) values.filterIsInstance<IntegerValue>().minOf { it.value }.v
        else (values.minOf { it.float }).v
    }

    function("max") { values ->
        if (values.none { it !is IntegerValue }) values.filterIsInstance<IntegerValue>().maxOf { it.value }.v
        else (values.maxOf { it.float }).v
    }

    unaryFunction("ln") { ln(it.float).v }
    function("log") {
        require(it.size == 2) { "Function 'log' requires two parameters" }
        val x = it[0].float
        val base = it[1].float
        log(x, base).v
    }
}

fun defaultFunctions() = buildMap { registerFunctions() }

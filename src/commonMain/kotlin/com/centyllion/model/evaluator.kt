package com.centyllion.model

import com.github.murzagalin.evaluator.DefaultFunctions
import com.github.murzagalin.evaluator.Evaluator
import com.github.murzagalin.evaluator.OneNumberArgumentFunction

interface FieldEvaluator {
    fun evaluateField(step: Int, index: Int, current: Float): Float
}

fun agentFunction(simulator: Simulator?) = object: OneNumberArgumentFunction("agent", 1) {
    override fun invokeInternal(arg: Number): Double = simulator?.let {
        val index = arg.toInt()
        val result = if (index >= 0 && index < it.agents.size) it.agents[index] else -1
        result.toDouble()
    } ?: -1.0
}

fun fieldFunction(field: Field, simulator: Simulator?) = object: OneNumberArgumentFunction("field${field.id}", 1) {
    override fun invokeInternal(arg: Number): Double = simulator?.let {
        val index = arg.toInt()
        // using fields that contains previous value for the field
        val values = it.fields[field.id] ?: return 0.0
        val result = if (index >= 0 && index < values.size) values[index] else 0f
        result.toDouble()
    } ?: 0.0
}

fun validateFormula(model: GrainModel, formula: String): String? {
    val evaluator = Evaluator(
        functions = DefaultFunctions.ALL
                + agentFunction(null)
                + model.fields.map { fieldFunction(it, null) }
    )
    val variables = buildMap {
        put("step", 0)
        put("index", 0)
        put("current", 0f)
    }
    return try {
        val parsed = evaluator.preprocessExpression(formula)
        evaluator.evaluateDouble(parsed, variables)
        null
    } catch (e: IllegalArgumentException) {
        e.message
    }
}

object SimpleFieldEvaluator: FieldEvaluator {
    override fun evaluateField(step: Int, index: Int, current: Float): Float = current

}

/**
 * Using project: https://github.com/murzagalin/multiplatform-expressions-evaluator
 */
class FormulaFieldEvaluator(
    val field: Field,
    val formula: String,
    val evaluator: Evaluator
): FieldEvaluator {

    private val expression = evaluator.preprocessExpression(formula)

    private val parameters = mutableMapOf<String, Number>().apply {
        put("step", 0)
        put("index", 0)
        put("current", 0f)
    }

    override fun evaluateField(step: Int, index: Int, current: Float): Float {
        parameters["step"] = step
        parameters["index"] = index
        parameters["current"] = current
        return evaluator.evaluateDouble(expression, parameters).toFloat()
    }

}
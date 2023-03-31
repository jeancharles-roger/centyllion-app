package com.centyllion.model

import com.github.murzagalin.evaluator.DefaultFunctions
import com.github.murzagalin.evaluator.Evaluator
import com.github.murzagalin.evaluator.Function

interface FieldEvaluator {
    fun evaluateField(step: Int, position: Position, current: Float): Float
}

fun agentFunction(simulator: Simulator?) = object: Function("agent", 2) {
    override fun invoke(vararg args: Any): Any {
        require(args.size == 2) { "$name should be called with x and y" }
        require(args.all { it is Int }) { "$name function requires all arguments to be integers" }
        if (simulator == null ) return -1.0
        val index = simulator.simulation.toIndex(args[0] as Int, args[1] as Int)
        val result = if (index >= 0 && index < simulator.agents.size) simulator.agents[index] else -1
        return result.toDouble()
    }
}

fun fieldFunction(field: Field, simulator: Simulator?) = object: Function("field${field.id}", 2) {
    override fun invoke(vararg args: Any): Any {
        require(args.size == 2) { "$name should be called with x and y" }
        require(args.all { it is Int }) { "$name function requires all arguments to be integers" }
        if (simulator == null ) return 0.0
        val index = simulator.simulation.toIndex(args[0] as Int, args[1] as Int)
        val values = simulator.fields[field.id] ?: return 0.0
        val result = if (index >= 0 && index < values.size) values[index] else 0f
        return result.toDouble()
    }
}

fun validateFormula(model: GrainModel, formula: String): String? {
    val evaluator = Evaluator(
        functions = DefaultFunctions.ALL
                + agentFunction(null)
                + model.fields.map { fieldFunction(it, null) }
    )
    val variables = buildMap {
        put("step", 0)
        put("x", 0)
        put("y", 0)
        put("z", 0)
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
    override fun evaluateField(step: Int, position: Position, current: Float): Float = current

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
        put("x", 0)
        put("y", 0)
        put("z", 0)
        put("current", 0f)
    }

    override fun evaluateField(step: Int, position: Position, current: Float): Float {
        parameters["step"] = step
        parameters["x"] = position.x
        parameters["y"] = position.y
        parameters["z"] = position.z
        parameters["current"] = current
        return evaluator.evaluateDouble(expression, parameters).toFloat()
    }

}
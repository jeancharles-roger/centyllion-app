package com.centyllion.expression

import com.centyllion.expression.BinaryOperator.*
import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt

sealed interface Value {

    operator fun plus(other: Value): Value
    operator fun minus(other: Value): Value
    operator fun times(other: Value): Value
    operator fun div(other: Value): Value
    operator fun rem(other: Value): Value

    fun and(other: Value): Value
    fun or(other: Value): Value

    operator fun compareTo(other: Value): Int

    operator fun not(): Value

    fun pow(other: Value): Value

    fun abs(): Value

    val int: Int

    val float: Float

    val boolean: Boolean

    companion object {
        val Zero: Value = IntegerValue(0)
        val True: Value = BooleanValue(true)
        val False: Value = BooleanValue(false)
    }
}

inline val Int.v get() = IntegerValue(this)
inline val Float.v get() = FloatValue(this)
inline val Boolean.v get() = BooleanValue(this)

@JvmInline
value class IntegerValue(val value: Int): Value {

    override fun plus(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value + other.value)
        is IntegerValue -> IntegerValue(value + other.value)
        is BooleanValue -> throw EvaluationException("Can't add boolean to number")
    }

    override fun minus(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value - other.value)
        is IntegerValue -> IntegerValue(value - other.value)
        is BooleanValue -> throw EvaluationException("Can't minus boolean to number")
    }

    override fun times(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value * other.value)
        is IntegerValue -> IntegerValue(value * other.value)
        is BooleanValue -> throw EvaluationException("Can't times boolean to number")
    }

    override fun div(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value / other.value)
        is IntegerValue -> IntegerValue(value / other.value)
        is BooleanValue -> throw EvaluationException("Can't div boolean to number")
    }

    override fun rem(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value % other.value)
        is IntegerValue -> IntegerValue(value % other.value)
        is BooleanValue -> throw EvaluationException("Can't rem boolean to number")
    }

    override fun or(other: Value): Value =
        throw EvaluationException("Can't or to a number")

    override fun and(other: Value): Value =
        throw EvaluationException("Can't and to a number")

    override fun compareTo(other: Value): Int = when (other) {
        is FloatValue -> value.toDouble().compareTo(other.value)
        is IntegerValue -> value.compareTo(other.value)
        is BooleanValue -> throw EvaluationException("Can't compare boolean to number")
    }

    override fun not(): Value =
        throw EvaluationException("Can't not a number")

    override fun pow(other: Value): Value = when(other) {
        is FloatValue -> float.pow(other.value).v
        is IntegerValue -> value.toDouble().pow(other.value).roundToInt().v
        is BooleanValue -> throw EvaluationException("Can't power boolean to number")
    }

    override fun abs(): Value = value.absoluteValue.v

    override val int: Int
        get() = value

    override val float: Float
        get() = value.toFloat()

    override val boolean: Boolean
        get() = throw EvaluationException("Can't translate an int to a boolean")

    override fun toString(): String = "$value"
}

@JvmInline
value class FloatValue(val value: Float): Value {

    override fun plus(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value + other.value)
        is IntegerValue -> FloatValue(value + other.value)
        is BooleanValue -> throw EvaluationException("Can't add boolean to number")
    }

    override fun minus(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value - other.value)
        is IntegerValue -> FloatValue(value - other.value)
        is BooleanValue -> throw EvaluationException("Can't minus boolean to number")
    }

    override fun times(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value * other.value)
        is IntegerValue -> FloatValue(value * other.value)
        is BooleanValue -> throw EvaluationException("Can't times boolean to number")
    }

    override fun div(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value / other.value)
        is IntegerValue -> FloatValue(value / other.value)
        is BooleanValue -> throw EvaluationException("Can't div boolean to number")
    }

    override fun rem(other: Value): Value = when (other) {
        is FloatValue -> FloatValue(value % other.value)
        is IntegerValue -> FloatValue(value % other.value)
        is BooleanValue -> throw EvaluationException("Can't rem boolean to number")
    }

    override fun or(other: Value): Value =
        throw EvaluationException("Can't or to a number")

    override fun and(other: Value): Value =
        throw EvaluationException("Can't and to a number")

    override fun compareTo(other: Value): Int = when (other) {
        is FloatValue -> value.compareTo(other.value)
        is IntegerValue -> value.compareTo(other.value.toDouble())
        is BooleanValue -> throw EvaluationException("Can't compare boolean to number")
    }

    override fun not(): Value =
        throw EvaluationException("Can't not a number")

    override fun pow(other: Value): Value = when(other) {
        is FloatValue ->  FloatValue(value.pow(other.value))
        is IntegerValue -> FloatValue(value.pow(other.value))
        is BooleanValue -> throw EvaluationException("Can't power boolean to number")
    }

    override fun abs(): Value = value.absoluteValue.v

    override val int: Int
        get() = throw EvaluationException("Can't translate a double to an int")

    override val float: Float
        get() = value

    override val boolean: Boolean
        get() = throw EvaluationException("Can't translate a double to a boolean")

    override fun toString(): String = "$value"
}

@JvmInline
value class BooleanValue(val value: Boolean): Value {

    override fun plus(other: Value): Value =
        throw EvaluationException("Can't add to a boolean")

    override fun minus(other: Value): Value =
        throw EvaluationException("Can't minus to a boolean")

    override fun times(other: Value): Value =
        throw EvaluationException("Can't times to a boolean")

    override fun div(other: Value): Value =
        throw EvaluationException("Can't div to a boolean")

    override fun rem(other: Value): Value =
        throw EvaluationException("Can't rem to a boolean")

    override fun or(other: Value): Value = when(other) {
        is BooleanValue -> BooleanValue(value || other.value)
        else -> throw EvaluationException("Can't or of a number")
    }

    override fun and(other: Value): Value = when(other) {
        is BooleanValue -> BooleanValue(value && other.value)
        else -> throw EvaluationException("Can't and of a number")
    }

    override fun compareTo(other: Value): Int = when (other) {
        is BooleanValue -> value.compareTo(other.value)
        else -> throw EvaluationException("Can't compare boolean to number")
    }

    override fun not(): Value = BooleanValue(!value)

    override fun pow(other: Value): Value =
        throw EvaluationException("Can't power a boolean")

    override fun abs(): Value =
        throw EvaluationException("Can't get absolute value for boolean")

    override val int: Int
        get() = throw EvaluationException("Can't translate a boolean to an int")

    override val float: Float
        get() = throw EvaluationException("Can't translate a boolean to a double")

    override val boolean: Boolean
        get() = value

    override fun toString(): String = "$value"
}


class EvaluationException(
    override val message: String
): Exception(message)

open class Evaluator(
    val variables: Map<String, Value> = emptyMap(),
    val functions: Map<String, (values: List<Value>) -> Value> = emptyMap()
) {

    fun check(node: Expression): List<ParsingError> {
        val check = CheckVisitor(this)
        node.accept(check)
        return check.errors.toList()
    }

    open fun function(name: String, args: List<Value>): Value =
        functions[name]?.let { it(args) } ?: throw EvaluationException("Can't find function '$name'")

    open fun variable(name: String): Value =
        variables[name] ?: throw EvaluationException("Can't find variable '$name'")

    fun evaluate(node: Expression): Value {
        return when (node) {
            is TernaryExpression -> {
                val condition = evaluate(node.condition)
                if (condition == Value.True) evaluate(node.ifThen)
                else evaluate(node.orElse)
            }

            is BinaryExpression -> {
                val left = evaluate(node.left)
                val right = evaluate(node.right)

                when (node.operator) {
                    Plus -> left + right
                    Minus -> left - right
                    Times -> left * right
                    Div -> left / right
                    Rem -> left % right
                    Or -> left.or(right)
                    And -> left.and(right)
                    GreaterOrEqualThan -> BooleanValue(left >= right)
                    GreaterThan -> BooleanValue(left > right)
                    LesserOrEqualThan -> BooleanValue(left <= right)
                    LesserThan -> BooleanValue(left < right)
                    Equal -> BooleanValue(left == right)
                    NotEqual -> BooleanValue(left != right)
                }
            }

            is UnaryExpression -> {
                val child = evaluate(node.child)
                when (node.operator) {
                    UnaryOperator.Plus -> Value.Zero + child
                    UnaryOperator.Minus -> Value.Zero - child
                    UnaryOperator.Not -> !child
                }
            }

            is ExponentExpression -> {
                val decimal = evaluate(node.decimal)
                val exponent = evaluate(node.exponent)
                decimal.pow(exponent)
            }

            is ParenthesisExpression -> evaluate(node.child)

            is FunctionCall -> function(node.name.content, node.arguments.map { evaluate(it) })

            is VariableCall -> variable(node.name.content)

            is BooleanLiteral -> node.value
            is NumberLiteral -> node.value
        }
    }
}

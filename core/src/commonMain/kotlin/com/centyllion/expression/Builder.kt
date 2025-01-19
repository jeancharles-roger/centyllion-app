package com.centyllion.expression

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@OptIn(ExperimentalContracts::class)
inline fun <T: TreeNode, B: Builder<T>> buildNode(builder: B, block: B.() -> Unit): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    block(builder)
    return builder.build()
}

interface Builder<out T: TreeNode>: MutableList<Node> {
    fun build(): T
}

class ModelProperty<T: Node>: ReadWriteProperty<MutableList<Node>, T> {
    private lateinit var value: T

    override fun getValue(thisRef: MutableList<Node>, property: KProperty<*>): T = value

    override fun setValue(thisRef: MutableList<Node>, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.add(value)
    }
}

class ModelOptional<T: Node>: ReadWriteProperty<MutableList<Node>, T?> {
    private var value: T? = null

    override fun getValue(thisRef: MutableList<Node>, property: KProperty<*>): T? = value

    override fun setValue(thisRef: MutableList<Node>, property: KProperty<*>, value: T?) {
        if (value != null) {
            this.value = value
            thisRef.add(value)
        }
    }
}

fun <T: Node> property() = ModelProperty<T>()
fun <T: Node> optional() = ModelOptional<T>()


class BooleanLiteralBuilder: Builder<BooleanLiteral>, MutableList<Node> by mutableListOf() {

    var literal by property<TokenNode>()

    override fun build(): BooleanLiteral = BooleanLiteral(
        children =  ArrayList(this),
        literal = literal,
        value = BooleanValue(literal.text().toBooleanStrict())
    )
}

class NumberLiteralBuilder: Builder<NumberLiteral>, MutableList<Node> by mutableListOf() {

    var literal by property<LiteralNode>()

    override fun build(): NumberLiteral = NumberLiteral(
        children =  ArrayList(this),
        literal = literal,
        value =
            if (literal.content.contains('.')) FloatValue(literal.content.toFloat())
            else IntegerValue(literal.content.toInt())

    )
}

class VariableCallBuilder: Builder<VariableCall>, MutableList<Node> by mutableListOf() {

    var name by property<IdentifierNode>()

    override fun build(): VariableCall = VariableCall(
        children =  ArrayList(this),
        name = name,
    )
}

class FunctionCallBuilder: Builder<FunctionCall>, MutableList<Node> by mutableListOf() {

    var name by property<IdentifierNode>()

    val argumentList: MutableList<Expression> = mutableListOf()
    fun argument(
        value: Expression?
    ): Boolean {
        if (value != null) {
            argumentList.add(value)
            add(value)
        }
        return value != null
    }

    override fun build(): FunctionCall = FunctionCall(
        children =  ArrayList(this),
        name = name,
        arguments = argumentList
    )
}

class ExponentExpressionBuilder: Builder<ExponentExpression>, MutableList<Node> by mutableListOf() {

    var decimal by property<Expression>()
    var exponent by property<Expression>()

    override fun build(): ExponentExpression = ExponentExpression(
        children =  ArrayList(this),
        decimal = decimal,
        exponent = exponent
    )
}

class ParenthesisExpressionBuilder: Builder<ParenthesisExpression>, MutableList<Node> by mutableListOf() {

    var child by property<Expression>()

    override fun build(): ParenthesisExpression = ParenthesisExpression(
        children =  ArrayList(this),
         child = child
    )
}

class UnaryExpressionBuilder: Builder<UnaryExpression>, MutableList<Node> by mutableListOf() {

    var operator by property<TokenNode>()
    var child by property<Expression>()

    override fun build(): UnaryExpression = UnaryExpression(
        children =  ArrayList(this),
        // TODO send error if not found
        operator = UnaryOperator.entries.find { it.text == operator.content }!!,
        child = child
    )
}

class BinaryExpressionBuilder: Builder<BinaryExpression>, MutableList<Node> by mutableListOf() {

    var left by property<Expression>()
    var operator by property<TokenNode>()
    var right by property<Expression>()

    override fun build(): BinaryExpression = BinaryExpression(
        children =  ArrayList(this),
        left = left,
        // TODO send error if not found
        operator = BinaryOperator.entries.find { it.text == operator.content }!!,
        right = right
    )
}

class TernaryExpressionBuilder: Builder<TernaryExpression>, MutableList<Node> by mutableListOf() {

    var condition by property<Expression>()
    var ifThen by property<Expression>()
    var orElse by property<Expression>()

    override fun build(): TernaryExpression = TernaryExpression(
        children =  ArrayList(this),
        condition = condition,
        ifThen = ifThen,
        orElse = orElse
    )
}

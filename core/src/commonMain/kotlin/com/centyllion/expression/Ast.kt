package com.centyllion.expression

data class Position(
    /** Character from the start of the file */
    val character: Int,
    /** Line */
    val line: Int,
    /** Starting column from the current line */
    val column: Int,
) {

    override fun toString() = "$line,$column"

    operator fun plus(content: String): Position {
        val delta = content.length
        val lastIndex = content.lastIndexOf('\n')
        return if (lastIndex < 0) copy(
            column = column + delta,
            character = character + delta
        ) else copy(
            line = line + content.count { it == '\n' },
            column = column + delta - lastIndex,
            character = character + delta
        )
    }
}

enum class Severity {
    Severe, Warning, Information, Hint
}

class ParsingException(
    val startPosition: Position,
    val endPosition: Position,
    override val message: String,
    val severity: Severity = Severity.Severe,
    val category: String = "Parsing",
) : Exception(message) {
    val length: Int = endPosition.character - startPosition.character

    val error get() = ParsingError(startPosition, endPosition, message, severity, category)
}

class ParsingError(
    val startPosition: Position,
    val endPosition: Position,
    val message: String,
    val severity: Severity = Severity.Severe,
    val category: String = "Parsing",
)

/** Interface for each node in the AST. */
interface Node {
    /**
     * Position of the last character of the node in the source file.
     * In the node constructor, position should given before previous space for correct placement.
     */
    val position: Position

    val endPosition: Position
        get() = position + text().substring(actualPreviousSpace.length)

    /** Empty space and comments before the node. */
    val previousSpace: String

    /**Previous space before this (searching in children if needed) */
    val actualPreviousSpace: String get() = previousSpace

    /** Node content (aadl for content) */
    val content: String

    fun text(): String = buildString { textTo(this) }

    /** Text for node (is equals to [previousSpace][content] ). */
    fun textTo(
        builder: StringBuilder = StringBuilder(),
        firstWithSpace: Boolean = true,
        parents: List<TreeNode> = emptyList(),
        modifier: ((List<TreeNode>, TreeNode) -> TreeNode?)? = null
    ) {
        // modifier doesn't apply to final Node only TreeNode
        if (firstWithSpace) builder.append(previousSpace)
        builder.append(content)
    }
}

/** Node that contains spaces and comments used for final node in an AST. */
data class SpaceAndCommentNode(
    override val position: Position,
    override val previousSpace: String,
) : Node {
    override val content: String = ""
}

data class TokenNode(
    override val position: Position,
    override val previousSpace: String,
    override val content: String,
) : Node

data class IdentifierNode(
    override val position: Position,
    override val previousSpace: String,
    override val content: String
) : Node

data class LiteralNode(
    override val position: Position,
    override val previousSpace: String,
    override val content: String
) : Node

interface TreeNode : Node {
    val children: List<Node>

    override val previousSpace: String
        get() = ""

    override val actualPreviousSpace: String
        get() = children.firstOrNull()?.actualPreviousSpace ?: previousSpace

    override val position: Position get() = children.first().position
    override val content: String get() = buildString { textTo(this, false) }

    override fun textTo(
        builder: StringBuilder,
        firstWithSpace: Boolean,
        parents: List<TreeNode>,
        modifier: ((List<TreeNode>, TreeNode) -> TreeNode?)?
    ) {
        val self = if (modifier != null) modifier(parents, this) else this
        if (self != null) {
            if (firstWithSpace) builder.append(self.previousSpace)
            self.children.forEachIndexed { index, node ->
                node.textTo(builder, firstWithSpace || index > 0, parents + self, modifier)
            }
        }
    }
}

sealed interface Expression: TreeNode {
    fun accept(visitor: Visitor)
}

data class TernaryExpression(
    override val children: List<Node>,
    val condition: Expression,
    val ifThen: Expression,
    val orElse: Expression,
): Expression {
    override fun accept(visitor: Visitor) {
        visitor.visitTernary(this)
    }
}

enum class BinaryOperator(val text: String) {
    Plus("+"),
    Minus("-"),
    Times("*"),
    Div("/"),
    Rem("%"),
    Or("||"),
    And("&&"),
    GreaterOrEqualThan(">="),
    GreaterThan(">"),
    LesserOrEqualThan("<="),
    LesserThan("<"),
    Equal("=="),
    NotEqual("!=")
}

data class BinaryExpression(
    override val children: List<Node>,
    val left: Expression,
    val operator: BinaryOperator,
    val right: Expression
): Expression {
    override fun accept(visitor: Visitor) {
        visitor.visitBinary(this)
    }
}


enum class UnaryOperator(val text: String) {
    Plus("+"),
    Minus("-"),
    Not("!"),
}

data class UnaryExpression(
    override val children: List<Node>,
    val operator: UnaryOperator,
    val child: Expression
): Expression {
    override fun accept(visitor: Visitor) {
        visitor.visitUnary(this)
    }
}

data class ExponentExpression(
    override val children: List<Node>,
    val decimal: Expression,
    val exponent: Expression,
): Expression {
    override fun accept(visitor: Visitor) {
        visitor.visitExponent(this)
    }
}

data class ParenthesisExpression(
    override val children: List<Node>,
    val child: Expression,
): Expression {
    override fun accept(visitor: Visitor) {
        visitor.visitParenthesis(this)
    }
}

data class FunctionCall(
    override val children: List<Node>,
    val name: IdentifierNode,
    val arguments: List<Expression>
): Expression {
    override fun accept(visitor: Visitor) {
        visitor.visitFunctionCall(this)
    }
}

data class VariableCall(
    override val children: List<Node>,
    val name: IdentifierNode,
): Expression {
    override fun accept(visitor: Visitor) {
        visitor.visitVariableCall(this)
    }
}

data class BooleanLiteral(
    override val children: List<Node>,
    val literal: TokenNode,
    val value: BooleanValue,
): Expression {
    override fun accept(visitor: Visitor) {
        visitor.visitBooleanLiteral(this)
    }
}

data class NumberLiteral(
    override val children: List<Node>,
    val literal: LiteralNode,
    val value: Value
): Expression {
    override fun accept(visitor: Visitor) {
        visitor.visitNumberLiteral(this)
    }
}


interface Visitor {
    fun visitTernary(node: TernaryExpression)
    fun visitBinary(node: BinaryExpression)
    fun visitUnary(node: UnaryExpression)
    fun visitExponent(node: ExponentExpression)
    fun visitParenthesis(node: ParenthesisExpression)
    fun visitFunctionCall(node: FunctionCall)
    fun visitVariableCall(node: VariableCall)
    fun visitBooleanLiteral(node: BooleanLiteral)
    fun visitNumberLiteral(node: NumberLiteral)
}
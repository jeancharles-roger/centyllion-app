package com.centyllion.expression

class CheckVisitor(val evaluator: Evaluator): Visitor {

    val errors = mutableListOf<ParsingError>()

    fun clear() {
        errors.clear()
    }

    private fun addError(node: Node, message: String) {
        errors.add(
            ParsingError(
            startPosition = node.position,
            endPosition = node.endPosition,
            message = message,
            severity = Severity.Severe,
            category = "Reference"
        )
        )
    }

    private fun visitChildren(node: Expression) {
        node.children
            .filterIsInstance<Expression>()
            .forEach { it.accept(this) }
    }

    override fun visitTernary(node: TernaryExpression) {
        visitChildren(node)
    }

    override fun visitBinary(node: BinaryExpression) {
        visitChildren(node)
    }

    override fun visitUnary(node: UnaryExpression) {
        visitChildren(node)
    }

    override fun visitExponent(node: ExponentExpression) {
        visitChildren(node)
    }

    override fun visitParenthesis(node: ParenthesisExpression) {
        visitChildren(node)
    }

    override fun visitFunctionCall(node: FunctionCall) {
        visitChildren(node)
        val name = node.name.content
        if (!evaluator.functions.containsKey(name)) {
            addError(node.name,"Function '$name' doesn't exist")
        }
    }

    override fun visitVariableCall(node: VariableCall) {
        visitChildren(node)
        val name = node.name.content
        if (!evaluator.variables.containsKey(name)) {
            addError(node.name,"Variable '$name' doesn't exist")
        }
    }

    override fun visitBooleanLiteral(node: BooleanLiteral) {
        visitChildren(node)
    }

    override fun visitNumberLiteral(node: NumberLiteral) {
        visitChildren(node)
    }

}
package com.centyllion.expression

fun String.parseExpression(): Expression {
    val parser = Parser(Lexer(StringInput(this)))
    return parser.parseExpression()
}

fun Parser.parseExpression(): Expression =
    buildNode(ParenthesisExpressionBuilder()) {
        child = readExpression()
        if (!lexer.input.eof) parsingFailure("EOF")
        add(finalNode())
    }

/**
 * ```
 * expression -> logic_or ( "?" expression ":" expression )?
 * ```
 */
fun Parser.readExpression(): Expression {
    val or = readLogicOr()
    return when {
        lexer.peekToken("?") -> buildNode(TernaryExpressionBuilder()) {
            condition = or
            add(readToken("?"))
            ifThen = readExpression()
            add(readToken(":"))
            orElse = readExpression()
        }

        else -> or
    }
}

private fun Parser.readBinary(
    tokens: List<String> = emptyList(),
    leftParser: Parser.() -> Expression,
    rightParser: Parser.() -> Expression = leftParser,
    recursive: Boolean = true
): Expression {
    var current = leftParser()
    while (lexer.peekOneOfTokens(tokens) != null) {
        current = buildNode(BinaryExpressionBuilder()) {
            left = current
            operator = readOneOfTokens(tokens)
            right = rightParser()
        }
        if (!recursive) break
    }
    return current
}

/**
 * ```
 * logic_or   -> logic_and ( "||" logic_and )*
 * ```
 */
private fun Parser.readLogicOr(): Expression = readBinary(
    tokens = listOf("||"), leftParser = Parser::readLogicAnd
)

/**
 * ```
 * logic_and  -> equality ( "&&" equality )*
 * ```
 */
private fun Parser.readLogicAnd(): Expression = readBinary(
    tokens = listOf("&&"), leftParser = Parser::readEquality
)


/**
 * ```
 * equality   -> comparison ( ( "!=" | "==" ) comparison )*
 * ```
 */
private fun Parser.readEquality(): Expression = readBinary(
    tokens = listOf("!=", "=="), leftParser = Parser::readComparison
)

/**
 * ```
 * comparison -> sum ( ( ">" | ">=" | "<" | "<=" ) sum )*
 * ```
 */
private fun Parser.readComparison(): Expression = readBinary(
    tokens = listOf(">=", "<=", ">", "<"), leftParser = Parser::readSum
)

/**
 * ```
 * sum        -> factor ( ( "-" | "+" ) factor )*
 * ```
 */
private fun Parser.readSum(): Expression = readBinary(
    tokens = listOf("-", "+"), leftParser = Parser::readFactor
)

/**
 * ```
 * factor     -> unary ( ( "/" | "*" | "%") unary )*
 * ```
 */
private fun Parser.readFactor(): Expression = readBinary(
    tokens = listOf("/", "*", "%"), leftParser = Parser::readUnary
)


/**
 * ```
 * unary      -> ( "!" | "-" | "+" ) unary | exponent
 * ```
 */
private fun Parser.readUnary(): Expression =
    if (lexer.peekOneOfTokens(unaryTokens) != null) buildNode(UnaryExpressionBuilder()) {
        operator = readOneOfTokens(unaryTokens)
        child = readUnary()
    } else readExponent()

private val unaryTokens = listOf("!", "-", "+")


/**
 * ```
 * exponent   -> terminal ( "^" unary )*
 * ```
 */
private fun Parser.readExponent(): Expression {
    var current = readTerminal()
    while (lexer.peekToken("^")) {
        current = buildNode(ExponentExpressionBuilder()) {
            decimal = current
            add(readToken("^"))
            exponent = readUnary()
        }
    }
    return current
}

/**
 * ```
 * terminal -> function | variable | number | boolean | parenthesis
 *
 * parenthesis -> '(' expression ')'
 * function   -> name "(" ( arguments )* ")"
 * arguments  -> expression ( ',' expression )*
 * variable   -> name
 * name       -> CHAR ( CHAR | DIGIT )*
 * boolean    -> "true" | "false" | "TRUE" | "FALSE"
 * number     -> DIGIT ( DIGIT )*
 * ```
 */
fun Parser.readTerminal(): Expression = when {
    lexer.peekToken("(") -> buildNode(ParenthesisExpressionBuilder()) {
        add(readToken("("))
        child = readExpression()
        add(readToken(")"))
    }

    peekIdentifier() -> {
        val identifier = readIdentifier()
        if (lexer.peekToken("(")) buildNode(FunctionCallBuilder()) {
            name = identifier
            add(readToken("("))
            if (!lexer.peekToken(")")) {
                argument(readExpression())
                while (lexer.peekToken(",")) {
                    add(readToken(","))
                    argument(readExpression())
                }
            }
            add(readToken(")"))
        } else buildNode(VariableCallBuilder()) {
            name = identifier
        }
    }

    lexer.peekOneOfTokens(booleanTokens) != null ->
        buildNode(BooleanLiteralBuilder()) {
            literal = readOneOfTokens(booleanTokens)
        }

    lexer.peekTokenWith(Lexer.digits) != null ->
        buildNode(NumberLiteralBuilder()) {
            literal = literal {
                lexer.tokenWith(Lexer.digits + '.')
                    ?: parsingFailure("number")
            }
        }

    else -> parsingFailure("terminal")
}

val booleanTokens = listOf("true", "false")
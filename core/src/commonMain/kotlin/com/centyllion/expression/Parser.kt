package com.centyllion.expression

class Parser(
    val lexer: Lexer
) {

    fun errorMessage(expected: String, actual: String = lexer.peekTokenNotWith(Lexer.lineFeeds) ?: "EOF") =
        "Expecting $expected at line ${lexer.input.currentLine}, column ${lexer.input.currentColumn} but is '$actual'"

    fun parsingFailure(expected: String, actual: String = lexer.peekTokenNotWith(Lexer.lineFeeds) ?: "EOF"): Nothing {
        val position = lexer.position
        val message = errorMessage(expected, actual)
        throw ParsingException(position, position + actual, message)
    }

    val identifierFirstCharacters: Array<Char> = Lexer.lowercaseLetters + Lexer.uppercaseLetters + '_'

    val identifierCharacters: Array<Char> = Lexer.lowercaseLetters + Lexer.uppercaseLetters + Lexer.digits + '_'

    private fun identifierPredicate(i: Int, c: Char) =
        (if (i <= 0) identifierFirstCharacters else identifierCharacters).any { it == c }

    fun peekIdentifier(): Boolean =
        lexer.peekTokenWith(::identifierPredicate) != null

    /** Reads an identifier. If the identifier is not valid it creates an error. */
    fun readIdentifier(): IdentifierNode =
        lexer.position.let { position ->
            lexer.tokenWith(predicate = ::identifierPredicate)
                ?.let { IdentifierNode(position, lexer.clearPreviousEmptySpace(), it) }
                ?: parsingFailure("variable")
        }

    /** Reads given token. If the token isn't found it creates an error. */
    fun readToken(token: String): TokenNode =
        lexer.position.let { position ->
            lexer.token(token)
                ?.let { TokenNode(position, lexer.clearPreviousEmptySpace(), it) }
                ?: parsingFailure(token)
        }

    /** Reads one of the given keyword. If none of the keywords if found, creates an error node. */
    fun readOneOfTokens(tokens: List<String>): TokenNode =
        lexer.position.let { position ->
            tokens.asSequence().mapNotNull { lexer.token(it) }.firstOrNull()
                // create the Node for keyword
                ?.let { TokenNode(position, lexer.clearPreviousEmptySpace(), it) }
            // no keyword found, create error node
                ?: parsingFailure("one of tokens '${tokens.joinToString()}'")
        }

    /** Creates a LiteralNode with given token block construction. */
    fun literal(token: () -> String): LiteralNode {
        val position = lexer.position
        val read = token()
        return LiteralNode(position, lexer.clearPreviousEmptySpace(), read)
    }

    /** A final node is empty but stores the non-coding end of the input (spaces and comments). */
    fun finalNode(): SpaceAndCommentNode = SpaceAndCommentNode(lexer.position, lexer.clearPreviousEmptySpace())
}
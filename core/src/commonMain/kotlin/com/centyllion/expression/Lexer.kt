package com.centyllion.expression

class Lexer(val input: Input) {

    val previousEmptySpace: StringBuilder = StringBuilder()

    val nextEmptySpace: StringBuilder = StringBuilder()

    init {
        skipWhitespacesAndComments()
        clearPreviousEmptySpace()
    }

    val possibleTokens: List<String> = listOf("==", "!=", ">=", "<=", "||", "&&")

    private fun isPossibleTokenPrefix(expected: String): Boolean = possibleTokens.any {
        it.length > expected.length && it.startsWith(expected) && it == input.peek(it.length)
    }

    fun clearPreviousEmptySpace(): String =
        previousEmptySpace.toString().also {
            previousEmptySpace.clear()
            previousEmptySpace.append(nextEmptySpace)
            nextEmptySpace.clear()
        }

    /** Skip white spaces and comments in input. */
    fun skipWhitespacesAndComments(): String {
        val result = buildString {
            append(input.skipWhitespaces())
            append(skipComments())
            append(input.skipWhitespaces())
        }
        nextEmptySpace.append(result)
        return result
    }

    fun skipComments(): String = buildString {
        while (peekToken("//") || peekToken("/*")) {
            when {
                peekToken("//") -> {
                    // read until the end of line
                    var current = input.peekAt()
                    while (current != null && current != '\n') {
                        append(current)
                        input.skip()
                        current = input.peekAt()
                    }
                }
                peekToken("/*") -> {
                    // starts with level at -1 since the first /* is read in the loop.
                    // expected exit level is 0
                    var level = -1
                    // read until the next '*/'
                    var current = input.peekAt()
                    while (current != null && (input.peek(2) != "*/" || level > 0) ) {
                        when (peekOneOfTokens(listOf("/*", "*/"))) {
                            "/*" -> level += 1
                            "*/" -> level -= 1
                        }
                        append(current)
                        input.skip()
                        current = input.peekAt()
                    }

                    repeat(2) {
                        append(input.peekAt())
                        input.skip()
                    }
                }
            }
            append(input.skipWhitespaces())
        }
    }

    fun peekToken(expected: String): Boolean {
        val token = input.peek(expected.length)
        return token == expected && !isPossibleTokenPrefix(expected)
    }

    /** Peeks one of given token. It returns the found token or null if none */
    fun peekOneOfTokens(tokens: List<String>): String? =
        // find first matching tokens (sequence allows to stop on first match)
        tokens.find { peekToken(it) }

    /**
     * Read given fixed token, return the token if found and advance.
     * It [skipWhitespacesAndComments] if token is found.
     */
    fun token(expected: String, skip: Boolean = true): String? {
        val token = input.peek(expected.length)
        return when {
            token != expected -> null
            isPossibleTokenPrefix(expected) -> null
            else -> {
                input.skip(expected.length)
                if (skip) skipWhitespacesAndComments()
                token
            }
        }
    }

    fun peekTokenNotWith(chars: Array<Char>): String? =
        peekTokenWith { _, c -> !chars.contains(c) }

    fun tokenNotWith(skip: Boolean = true, predicate: (Int, Char) -> Boolean): String? =
        tokenWith(skip) { i, c -> !predicate(i, c)}

    fun peekTokenWith(chars: Array<Char>): String? =
        peekTokenWith { _, c -> chars.contains(c) }

    fun peekTokenWith(predicate: (Int, Char) -> Boolean): String? {
        var delta = 0
        var current = input.peekAt(delta)
        // checks the peeked size still matches the predicate
        while (current != null && predicate(delta, current)) {
            delta += 1
            current = input.peekAt(delta)
        }
        // when the size > 1, the pattern matched at least one character, none otherwise.
        return if (delta > 0) input.peek(delta) else null
    }

    fun tokenWith(chars: Array<Char>): String? =
        tokenWith { _, c -> chars.contains(c) }

    fun tokenWith(skip: Boolean = true, predicate: (Int, Char) -> Boolean): String? {
        val result = StringBuilder()
        var current = input.peekAt()
        while (current != null && predicate(result.length, current)) {
            result.append(current)
            input.skip()
            current = input.peekAt()
        }
        return if (result.isEmpty()) null else {
            if (skip) skipWhitespacesAndComments()
            result.toString()
        }
    }

    val position: Position
        get()  {
            val emptySpace = nextEmptySpace.toString()
            val length = emptySpace.length
            val lastLf = emptySpace.lastIndexOf('\n')
            return Position(
                input.current - length,
                input.currentLine - emptySpace.count { it == '\n' },
                input.currentColumn - if (lastLf == -1) length else length - lastLf,
                )
        }
    
    companion object {
        val lineFeeds = arrayOf('\n', '\r')

        val lowercaseLetters: Array<Char> = arrayOf(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        )

        val uppercaseLetters: Array<Char> = arrayOf(
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        )

        val digits: Array<Char> = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    }
}
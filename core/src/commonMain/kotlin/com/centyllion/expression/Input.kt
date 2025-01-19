package com.centyllion.expression

interface Input {

    val eof get() = peekAt() == null

    val current: Int
    
    val currentLine: Int
    
    val currentColumn: Int
    
    fun peekAt(index: Int = 0): Char?

    fun peek(length: Int): String?

    fun skip(length: Int = 1)

    /** Skip white spaces in input. */
    fun skipWhitespaces(): String = buildString {
        var current = peekAt()
        while (' ' == current || '\n' == current || '\r' == current || '\t' == current) {
            append(current)
            skip()
            current = peekAt()
        }
    }

}

class StringInput(val content: String): Input {
    override var current: Int = 0
    override var currentLine: Int = 0
    override var currentColumn: Int = 0

    override fun peekAt(index: Int): Char? = content.getOrNull(current + index)

    override fun peek(length: Int): String? {
        if (length <= 0 || (current + length ) > content.length) return null
        return content.substring(current, current + length)
    }

    override fun skip(length: Int) {
        val peeked = peek(length)

        // count line feed in current text
        currentLine += peeked?.count { it == '\n' } ?: 0
        // last line feed index
        val lastLf = peeked?.lastIndexOf('\n') ?: -1
        // if there is no line feed (-1) add length otherwise set to length since last line feed
        currentColumn = if ( lastLf == -1) currentColumn + length else length - lastLf
        current += length
    }

}
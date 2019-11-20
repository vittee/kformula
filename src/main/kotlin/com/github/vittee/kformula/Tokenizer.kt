package com.github.vittee.kformula

import com.github.vittee.kformula.TokenType.*

internal data class Token(val text: String, val type: TokenType, val literal: Any? = null)

class InvalidChar(c: Char) : RuntimeException("Invalid character $c")

internal class Tokenizer(private val source: String) {
    private var pos = 0

    var token: Token? = null
        private set

    private val tokenBuffer = StringBuilder()

    fun available() = source.length - pos;

    private fun readToken() {
        killToken()
        consumeToken()
    }

    fun testAny(types: Set<TokenType>) = ensureToken()?.let {
        when {
            types.contains(it.type) -> it.type
            else -> NONE
        }
    } ?: NONE

    fun testDelete(type: TokenType) = ensureToken()
        ?.takeIf { it.type == type }
        ?.let {
            killToken()
            true
        }
        ?: false

    fun testDeleteAny(types: Set<TokenType>) = ensureToken()
        ?.takeIf { types.contains(it.type) }
        ?.let {
            killToken()
            it.type
        }
        ?: NONE

    fun testNumber() = ensureToken()?.let { t -> t.text.isNotEmpty() && (t.type == NUMBER) } ?: false

    fun testName() = ensureToken()?.let { t -> t.text.isNotEmpty() && (t.type == NAME || t.type == VARIABLE) } ?: false

    fun killToken() {
        token = null
        tokenBuffer.clear()
    }

    fun hasTokens() = ensureToken() != null

    private fun ensureToken(): Token? {
        if (token == null) {
            readToken()
        }

        return token
    }

    fun skipWhitespace() {
        while (available() > 0 && peek().isWhitespace()) {
            advance()
        }
    }

    private fun next(): Char {
        skipWhitespace()
        return advance()
    }

    private fun match(expect: Char): Boolean {
        val next = peek()

        if (next == expect) {
            tokenBuffer.append(next)
            advance()
            return true
        }

        return false
    }

    private fun consumeToken() {
        if (available() <= 0) {
            return
        }

        when (val c = next()) {
            ' ', '\t', '\r', '\n'  -> { }
            '+', '-', '*', '/', '^', '(', ')', ',' -> tokenBuffer.append(c)
            '!','>', '='  -> {
                tokenBuffer.append(c)
                match('=')
            }
            '<' -> {
                tokenBuffer.append(c)
                match('>') || match('=')
            }
            '.' -> {
                // TODO: Dot, DotDot
            }
            in '0'..'9' -> {
                tokenBuffer.append(c)
                number()
                token = Token(tokenBuffer.toString(), TokenType.NUMBER, tokenBuffer.toString().toBigDecimal())
                return
            }
            '$','%' -> {
                tokenBuffer.append(c)
                variable()
                token = Token(tokenBuffer.toString(), TokenType.VARIABLE)
                return
            }
            '_',
            in 'a'..'z',
            in 'A'..'Z' -> {
                tokenBuffer.append(c)
                name()

                val type = when (val tt = currentTokenType()) {
                    NONE -> TokenType.NAME
                    else -> tt
                }
                token = Token(tokenBuffer.toString(), type)
                return
            }
            Char.MIN_VALUE -> {
                return
            }
            else -> {
                throw InvalidChar(c)
            }
        }

        token = Token(tokenBuffer.toString(), currentTokenType())
    }

    private fun number() {
        loop@ while(available() > 0) {
            when (peek()) {
                in '0'..'9' ->  tokenBuffer.append(advance())
                '.' -> {
                    tokenBuffer.append(advance())
                    while (available() > 0 && peek() in '0'..'9') {
                        tokenBuffer.append(advance())
                    }
                }
                else -> break@loop
            }
        }
    }

    private fun variable() {
        while (available() > 0 && peek().isValidVariableChar()) {
            tokenBuffer.append(advance())
        }
    }

    private fun name() {
        while (available() > 0 && peek().isValidName()) {
            tokenBuffer.append(advance())
        }
    }

    private fun currentTokenType(): TokenType {
        if (tokenBuffer.isEmpty()) {
            return TokenType.NAME
        }

        return when (tokenBuffer.toString().toLowerCase()) {
            "+" -> TokenType.PLUS
            "-" -> TokenType.MINUS
            "*" -> TokenType.TIMES
            "/" -> TokenType.DIVIDE
            "^" -> TokenType.EXPONENT
            "(" -> TokenType.B_LEFT
            ")" -> TokenType.B_RIGHT
            "," -> TokenType.COMMA
            "!" -> TokenType.EXCLAMATION
            "!=" -> TokenType.EX_EQ
            ">" -> TokenType.GREATER
            ">=" -> TokenType.GREATER_EQ
            "=" -> TokenType.EQUAL
            "==" -> TokenType.EQUAL_EQUAL
            "<" -> TokenType.LESS
            "<=" -> TokenType.LESS_EQ
            "<>" -> TokenType.NOT_EQ
            "mod" -> TokenType.MOD
            "and" -> TokenType.AND
            "or" -> TokenType.OR
            "not" -> TokenType.NOT
            "true" -> TokenType.TRUE
            "false" -> TokenType.FALSE
            // TODO: in
            else -> TokenType.NONE
        }
    }

    private fun advance() = if (pos < source.length) source[pos++] else Char.MIN_VALUE

    private fun peek() = if (pos < source.length) source[pos] else Char.MIN_VALUE

    private fun Char.isValidVariableChar() = (this.toLong() > 127 && this.toInt() != 160)
            || (this in 'A'..'Z')
            || (this in 'a'..'z')
            || (this in '0'..'9')
            || (this == '.')
            || (this == '_')

    private fun Char.isValidName() = (this in 'A'..'Z')
            || (this in 'a'..'z')
            || (this in '0'..'9')
            || (this == '.')
            || (this == '_')
}


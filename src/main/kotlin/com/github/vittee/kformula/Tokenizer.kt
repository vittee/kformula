package com.github.vittee.kformula

import com.github.vittee.kformula.TokenType.*

internal data class Token(val text: String, val type: TokenType, val literal: Any? = null)

class ParseError(s: String) : RuntimeException(s)

class InvalidChar(c: Char) : RuntimeException("Invalid character $c")

internal class Tokenizer(private val source: String) {
    private var pos = 0

    var token: Token? = null
        private set

    private val tokenBuffer = StringBuilder()

    fun available() = source.length - pos

    private fun readToken() {
        killToken()
        consumeToken()
    }

    fun testAny(vararg types: TokenType) = ensureToken()?.let {
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

    fun testDeleteAny(vararg types: TokenType) = ensureToken()
        ?.takeIf { types.contains(it.type) }
        ?.let {
            killToken()
            it.type
        }
        ?: NONE

    fun testNumber() = ensureToken()?.let { t -> t.text.isNotEmpty() && (t.type == NUMBER) } ?: false
    fun test(type: TokenType) = ensureToken()?.let { tok -> (tok.type == type) } ?: false

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
                if (!match('.')) {
                    throw ParseError("DOT expected, but ${peek()} found")
                }

                killToken()
                tokenBuffer.append("..")
            }
            in '0'..'9' -> {
                tokenBuffer.append(c)
                number()
                token = Token(tokenBuffer.toString(), NUMBER, tokenBuffer.toString().toBigDecimal())
                return
            }
            '$','%' -> {
                tokenBuffer.append(c)
                variable()
                token = Token(tokenBuffer.toString(), VARIABLE)
                return
            }
            '_',
            in 'a'..'z',
            in 'A'..'Z' -> {
                tokenBuffer.append(c)
                name()

                val type = when (val tt = currentTokenType()) {
                    NONE -> NAME
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
                    advance()

                    if (peek() == '.') { // DOT DOT
                        pos--
                        break@loop
                    }

                    tokenBuffer.append('.')
                    while (available() > 0 && peek() in '0'..'9') {
                        tokenBuffer.append(advance())
                    }

                    break@loop
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
            return NAME
        }

        return when (tokenBuffer.toString().toLowerCase()) {
            "+" -> PLUS
            "-" -> MINUS
            "*" -> TIMES
            "/" -> DIVIDE
            "^" -> EXPONENT
            "(" -> B_LEFT
            ")" -> B_RIGHT
            "," -> COMMA
            "!" -> EXCLAMATION
            "!=" -> EX_EQ
            ">" -> GREATER
            ">=" -> GREATER_EQ
            "=" -> EQUAL
            "==" -> EQUAL_EQUAL
            "<" -> LESS
            "<=" -> LESS_EQ
            "<>" -> NOT_EQ
            ".." -> DOT_DOT
            "mod" -> MOD
            "and" -> AND
            "or" -> OR
            "not" -> NOT
            "true" -> TRUE
            "false" -> FALSE
            "if" -> IF
            "then" -> THEN
            "else" -> ELSE
            "in" -> IN
            "between" -> BETWEEN
            else -> NONE
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


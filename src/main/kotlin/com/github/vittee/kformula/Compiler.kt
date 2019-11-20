package com.github.vittee.kformula

import com.github.vittee.kformula.TokenType.*
import java.math.BigDecimal

open class CompileError(s: String) : RuntimeException("Parse Error: $s")
class NoMoreExpressionError : CompileError("End of expression expected")
class NeverError : CompileError("Should not happen")

class Compiler {
    private var tokenizer = Tokenizer("")

    fun compile(source: String): Expr {
        tokenizer = Tokenizer(source)
        val e = readExpr()

        tokenizer.token?.let { throw NoMoreExpressionError()  }

        tokenizer.killToken()
        tokenizer.skipWhitespace()
        if (tokenizer.available() > 0) {
            throw NoMoreExpressionError()
        }

        return e
    }

    private fun readExpr(): Expr {
        var left = readExprAdd()
        loop@ do {
            val tt = tokenizer.testDeleteAny(setOf(EQUAL, EQUAL_EQUAL, NOT_EQ, EX_EQ, LESS, LESS_EQ, GREATER, GREATER_EQ, IN))
            left = when (tt) {
                NONE -> break@loop
                IN -> readInExpr(left)
                else -> {
                    val right = readExprAdd()

                    when (tt) {
                        EQUAL, EQUAL_EQUAL -> EqualExpr(left, right)
                        NOT_EQ, EX_EQ -> NotEqualExpr(left, right)
                        LESS -> LessExpr(left, right)
                        LESS_EQ -> LessEqualExpr(left, right)
                        GREATER -> GreaterExpr(left, right)
                        GREATER_EQ -> GreaterEqualExpr(left, right)
                        else -> throw NeverError()
                    }
                }
            }
        } while (true)

        return left
    }

    private fun readExprAdd(): Expr {
        var left = readExprMulti()

        loop@ do {
            val tt = tokenizer.testDeleteAny(setOf(PLUS, MINUS, OR, NOT))
            left = when (tt) {
                NONE -> break@loop
                NOT -> readNotInExpr(left)
                else -> {
                    val right = readExprMulti()
                    when (tt) {
                        PLUS -> AddExpr(left, right)
                        MINUS -> SubtractExpr(left, right)
                        OR -> LogicalOrExpr(left, right)
                        else -> throw NeverError()
                    }
                }
            }
        } while (true)

        return left
    }

    private fun readExprMulti(): Expr {
        var left = readTerm()
        do {
            val tt = tokenizer.testDeleteAny(setOf(TIMES, DIVIDE, MOD, AND, EXPONENT))
            if (tt == NONE) break

            val right = readTerm()

            left = when (tt) {
                TIMES -> MultiplyExpr(left, right)
                DIVIDE -> DivideExpr(left, right)
                MOD -> ModuloExpr(left, right)
                AND -> LogicalAndExpr(left, right)
                EXPONENT -> PowerExpr(left, right)
                else -> throw NeverError()
            }
        } while (true)

        return left
    }

    private fun readTerm(): Expr = when (tokenizer.testAny(setOf(PLUS, MINUS, NOT, EXCLAMATION, TRUE, FALSE, B_LEFT, IF))) {
        PLUS -> {
            tokenizer.killToken()
            readTerm()
        }
        MINUS -> {
            tokenizer.killToken()
            readNegation()
        }
        EXCLAMATION, NOT -> {
            tokenizer.killToken()
            readNotTerm()
        }
        TRUE -> {
            tokenizer.killToken()
            NumberExpr(BigDecimal.ONE)
        }
        FALSE -> {
            tokenizer.killToken()
            NumberExpr(BigDecimal.ZERO)
        }
        B_LEFT -> {
            tokenizer.killToken()
            readBracket()
        }
        IF -> {
            tokenizer.killToken()
            readIfExpr()
        }
        else -> when {
            tokenizer.testName() -> readName()
            else -> readImmediate()
        }
    }

    private fun readNegation() = NegateExpr(readTerm())

    private fun readNotTerm() = LogicalNotExpr(readTerm())

    private fun readIfExpr(): Expr {
        val cond = readExpr()

        tokenizer.testDelete(THEN)

        val trueExpr = readExpr()
        val falseExpr = when {
            tokenizer.testDelete(ELSE) -> readExpr()
            else -> NumberExpr(BigDecimal.ZERO)
        }

        return IfThenElseValueExpr(cond, trueExpr, falseExpr)
    }

    private fun readInExpr(left: Expr): Expr {
        val hasParenthesis = tokenizer.testDelete(B_LEFT)

        val begin = readExpr()

        if (tokenizer.testDeleteAny(setOf(BETWEEN, DOT_DOT)) == NONE) {
            throw CompileError("BETWEEN/.. expected")
        }

        val end = if (hasParenthesis) readBracket() else readExpr()

        return InExpr(left, begin, end)
    }

    private fun readNotInExpr(left: Expr): Expr {
        if (!tokenizer.testDelete(IN)) {
            throw CompileError("IN expected")
        }

        return NotInExpr(readInExpr(left))
    }

    private fun readBracket(): Expr {
        val e = readExpr()
        if (!tokenizer.testDelete(B_RIGHT)) {
            throw CompileError(""") expected""")
        }

        return e
    }

    private fun readName(): Expr {
        if (!tokenizer.testName()) {
            throw CompileError("Name expected")
        }

        TODO("Name lookup")
    }

    private fun readImmediate(): Expr {
        if (!tokenizer.testNumber()) {
            throw CompileError("Number or expression expected")
        }

        val v = tokenizer.token!!.literal as BigDecimal
        tokenizer.killToken()

        return NumberExpr(v)
    }
}
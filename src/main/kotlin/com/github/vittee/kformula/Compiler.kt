package com.github.vittee.kformula

import com.github.vittee.kformula.TokenType.*
import java.math.BigDecimal

class CompileError(s: String) : RuntimeException("Parse Error: $s")

class Compiler {
    private var tokenizer = Tokenizer("")

    fun compile(source: String): Expr {
        tokenizer = Tokenizer(source)
        return readExpr()
    }

    private fun readExpr(): Expr {
        return readExprAdd()
    }

    private fun readExprAdd(): Expr {
        var left = readExprMulti()
        do {
            val tt = tokenizer.testDeleteAny(setOf(PLUS, MINUS, OR))
            if (tt == NONE) break

            val right = readExprMulti()

            left = when (tt) {
                PLUS -> AddExpr(left, right)
                MINUS -> SubtractExpr(left, right)
                OR -> {
                    TODO("expr or")
                }
                else -> TODO("????")
            }
        } while(true)

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
                AND -> TODO("expr and")
                EXPONENT -> PowerExpr(left, right)
                else -> TODO("???")
            }
        } while (true)

        return left
    }

    private fun readTerm(): Expr = when (tokenizer.testAny(setOf(PLUS, MINUS, NOT, TRUE, FALSE, B_LEFT))) {
        PLUS -> {
            tokenizer.killToken()
            readTerm()
        }
        MINUS -> {
            tokenizer.killToken()
            readNegation()
        }
        NOT -> {
            TODO("readNotTerm")
        }
        TRUE -> {
            TODO("create 1")
        }
        FALSE -> {
            TODO("create 0")
        }
        B_LEFT -> {
            tokenizer.killToken()
            readBracket()
        }
        // TODO: IF -> ReadIfExpr
        else -> when {
            tokenizer.testName() -> readName()
            else -> readImmediate()
        }
    }

    private fun readNegation() = NegateExpr(readTerm())

    private fun readIfExpr() {

    }

    private fun readBracket(): Expr {
        val e = readExpr()
        if (!tokenizer.testDelete(B_RIGHT)) {
            throw CompileError(""") expected""")
        }

        return e
    }

    private fun readName(): Expr {
        TODO("Name lookup")
    }

    private fun readImmediate(): Expr {
        if (!tokenizer.testNumber()) {
            throw CompileError("Number expected")
        }

        val v = tokenizer.token!!.literal as BigDecimal
        tokenizer.killToken()

        return NumberExpr(v)
    }
}
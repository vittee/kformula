package com.github.vittee.kformula

import com.github.vittee.kformula.TokenType.*
import java.math.BigDecimal

open class CompileError(s: String) : RuntimeException("Parse Error: $s")
class NoMoreExpressionError : CompileError("End of expression expected")
class NeverError : CompileError("Should not happen")

class Compiler(private val table: SymbolTable<Symbol> = SymbolTable()) {
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
            val tt = tokenizer.testDeleteAny(EQUAL, EQUAL_EQUAL, NOT_EQ, EX_EQ, LESS, LESS_EQ, GREATER, GREATER_EQ, IN)
            left = when (tt) {
                NONE -> break@loop
                IN -> readInBetweenExpr(left)
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
            val tt = tokenizer.testDeleteAny(PLUS, MINUS, OR, NOT, EXCLAMATION)
            left = when (tt) {
                NONE -> break@loop
                NOT, EXCLAMATION -> readNotInExpr(left)
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
            val tt = tokenizer.testDeleteAny(TIMES, DIVIDE, MOD, AND, EXPONENT)
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

    private fun readTerm(): Expr = when (tokenizer.testAny(PLUS, MINUS, NOT, EXCLAMATION, TRUE, FALSE, B_LEFT, IF)) {
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
            tokenizer.testVariable() -> readVariable()
            else -> readImmediate()
        }
    }

    private fun readNegation() = NegateExpr(readTerm())

    private fun readNotTerm() = LogicalNotExpr(readTerm())

    private fun readIfExpr(): Expr {
        if (!tokenizer.testDelete(B_LEFT)) {

            return readIfThenElse()
        }

        val cond = readExpr()
        if (!tokenizer.testDelete(COMMA)) {
            throw CompileError("Comma expected")
        }

        val trueExpr = readExpr()

        val falseExpr = when {
            tokenizer.testDelete(COMMA) -> readExpr()
            else -> NumberExpr(BigDecimal.ZERO)
        }

        if (!tokenizer.testDelete(B_RIGHT)) {
            throw CompileError(") expected")
        }

        return IfThenElseValueExpr(cond, trueExpr, falseExpr)
    }

    private fun readIfThenElse(): Expr {
        val cond = readExpr()

        tokenizer.testDelete(THEN)

        val trueExpr = readExpr()
        val falseExpr = when {
            tokenizer.testDelete(ELSE) -> readExpr()
            else -> NumberExpr(BigDecimal.ZERO)
        }

        return IfThenElseValueExpr(cond, trueExpr, falseExpr)
    }

    private fun readInBetweenExpr(left: Expr): InBetweenExpr {
        val hasParenthesis = tokenizer.testDelete(B_LEFT)

        val begin = readExpr()

        if (tokenizer.testDeleteAny(BETWEEN, DOT_DOT) == NONE) {
            throw CompileError("BETWEEN/.. expected")
        }

        val end = if (hasParenthesis) readBracket() else readExpr()

        return InBetweenExpr(left, begin, end)
    }

    private fun readNotInExpr(left: Expr): NotInBetweenExpr {
        if (!tokenizer.testDelete(IN)) {
            throw CompileError("IN expected")
        }

        return NotInBetweenExpr(readInBetweenExpr(left))
    }

    private fun readBracket(): Expr {
        val e = readExpr()
        if (!tokenizer.testDelete(B_RIGHT)) {
            throw CompileError(") expected")
        }

        return e
    }

    private fun readName(): Expr {
        if (!tokenizer.testName()) {
            throw CompileError("Name expected")
        }

        val name = tokenizer.token!!.text
        val symbol = table.find<Symbol>(name) ?: throw CompileError("Symbol $name is not defined")

        return when (symbol) {
            is ConstValueSymbol -> readConstant()
            is FunctionSymbol -> readFunc(symbol)
            else -> throw CompileError("Symbol of type ${symbol.javaClass.name} could not be used")
        }
    }

    private fun readFunc(symbol: FunctionSymbol): FunctionExpr {
        tokenizer.killToken()

        if (!tokenizer.testDelete(B_LEFT)) {
            throw CompileError("( expected")
        }

        val args = mutableListOf<Expr>()

        if (!tokenizer.testDelete(B_RIGHT)) {
            do {
                args += readExpr()
            } while (tokenizer.testDelete(COMMA) && tokenizer.hasTokens())

            if (!tokenizer.testDelete(B_RIGHT)) {
                throw CompileError(") expected")
            }
        }

        val hasVariadic = symbol.params.last() is FunctionVariadicParameterSymbol
        val count = symbol.params.count + (if (hasVariadic) -1 else 0)

        // fill default values
        if (args.size < count) {
            for (i in args.size until count) {
                symbol.params[i].default?.let {
                    args += NumberExpr(it)
                }
            }
        }

        if (args.size < count && count > 0) {
            throw CompileError("At least $count arguments are required, got ${args.size}, argument named \"${symbol.params[args.size].name}\" is missing")
        }

        if (!hasVariadic && args.size > count) {
            when  {
                count > 0 -> "Too many arguments, only $count are required, but got ${args.size}"
                else -> "Too many arguments, none are required, but got ${args.size}"
            }.let{ throw CompileError(it) }

        }

        return FunctionExpr(symbol).apply {
            val static = when {
                hasVariadic -> args.subList(0, count)
                else -> args
            }

            static.forEachIndexed { index, expr ->
                when (val param = symbol.params[index]) {
                    is FunctionLazyParameterSymbol -> FunctionLazyArgumentExpr(param, expr)
                    else -> FunctionArgumentExpr(param, expr.eval())
                }.let(::addArgument)
            }

            if (hasVariadic) {
                FunctionVariadicArgumentExpr(
                    symbol.params.last() as FunctionParameterSymbol,
                    args.slice(count until args.size)
                ).let(::addArgument)
            }

            prepare()
        }
    }

    private fun readConstant(): ValueSymbolExpr {
        if (!tokenizer.testName()) {
            throw CompileError("Constant name expected")
        }

        val name = tokenizer.token!!.text
        val symbol = table.find<ConstValueSymbol>(name) ?: throw CompileError("Constant $name does not exist")

        tokenizer.killToken()

        if (tokenizer.test(B_LEFT)) {
            throw CompileError("Constant $name could not be called")
        }

        return ValueSymbolExpr(symbol)
    }

    private fun readVariable(): ValueSymbolExpr {
        if (!tokenizer.testVariable()) {
            throw CompileError("Variable expected")
        }

        val name = tokenizer.token!!.text
        val symbol = table.find<DataSymbol>(name) ?: throw CompileError("Variable $name does not exist")

        tokenizer.killToken()

        if (tokenizer.test(B_LEFT)) {
            throw CompileError("Variable $name could not be called")
        }

        return ValueSymbolExpr(symbol)
    }

    private fun readImmediate(): NumberExpr {
        if (!tokenizer.testNumber()) {
            throw CompileError("Number or expression expected")
        }

        val v = tokenizer.token!!.literal as BigDecimal
        tokenizer.killToken()

        return NumberExpr(v)
    }
}
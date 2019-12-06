package com.github.vittee.kformula

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.pow

sealed class Expr {
    abstract fun eval(): BigDecimal

    override fun equals(other: Any?) = this === other

    override fun hashCode() = System.identityHashCode(this)
}

class RootExpr(private val child: Expr) : Expr() {
    override fun eval() = child.eval()
        .stripTrailingZeros()
        .toPlainString()
        .toBigDecimal()

    fun safeEval(): BigDecimal = try {
        eval()
    }
    catch (e: Exception) {
        BigDecimal.ZERO
    }
}

internal val Expr.isPercentage: Boolean
    get() = this is ICanBePercentage && this.isPercentage

internal open class NumberExpr(private val value: BigDecimal) : Expr() {
    override fun eval() = value
}

internal class PercentageExpr(value: BigDecimal) : NumberExpr(value), ICanBePercentage {
    override val isPercentage = true
}

internal abstract class UnaryExpr(val right: Expr) : Expr(), ICanBePercentage {
    override val isPercentage = right.isPercentage
}

internal abstract class BinaryExpr(val left: Expr, val right: Expr) : Expr()

internal abstract class ArithmeticBinaryExpr(left: Expr, right: Expr) : BinaryExpr(left, right), ICanBePercentage {
    override val isPercentage = left.isPercentage
}

internal abstract class RelationalOperatorExpr(left: Expr, right: Expr) : BinaryExpr(left, right)

internal abstract class LogicalOperatorExpr(left: Expr, right: Expr) : BinaryExpr(left, right)

internal class NegateExpr(right: Expr) : UnaryExpr(right) {
    override fun eval(): BigDecimal = right.eval().negate()
}

internal class AddExpr(left: Expr, right: Expr): ArithmeticBinaryExpr(left, right) {
    override fun eval() = let {
        val (l, r) = left.eval() to right.eval()

        when {
            !left.isPercentage && right.isPercentage -> l * (1 + r)
            else -> l + r
        }
    }
}

internal class SubtractExpr(left: Expr, right: Expr): ArithmeticBinaryExpr(left, right) {
    override fun eval() = let {
        val (l, r) = left.eval() to right.eval()

        when {
            !left.isPercentage && right.isPercentage -> l * (1 - r)
            else -> l - r
        }
    }
}

internal class MultiplyExpr(left: Expr, right: Expr): ArithmeticBinaryExpr(left, right) {
    override fun eval() = left.eval() * right.eval()
}

internal class DivideExpr(left: Expr, right: Expr): ArithmeticBinaryExpr(left, right) {
    override fun eval(): BigDecimal = left.eval().divide(right.eval(), MathContext.DECIMAL64)
}

internal class ModuloExpr(left: Expr, right: Expr): ArithmeticBinaryExpr(left, right) {
    override fun eval(): BigDecimal = left.eval().remainder(right.eval(), MathContext.DECIMAL64)
}

internal class PowerExpr(left: Expr, right: Expr): ArithmeticBinaryExpr(left, right) {
    override fun eval() = left.eval() pow right.eval()
}

internal class EqualExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, right) {
    override fun eval(): BigDecimal = (left.eval() == right.eval()).toBigDecimal()
}

internal class NotEqualExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, right) {
    override fun eval(): BigDecimal = (left.eval() != right.eval()).toBigDecimal()
}

internal class LessExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, right) {
    override fun eval(): BigDecimal = (left.eval() < right.eval()).toBigDecimal()
}

internal class LessEqualExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, right) {
    override fun eval(): BigDecimal = (left.eval() <= right.eval()).toBigDecimal()
}

internal class GreaterExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, right) {
    override fun eval(): BigDecimal = (left.eval() > right.eval()).toBigDecimal()
}

internal class GreaterEqualExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, right) {
    override fun eval(): BigDecimal = (left.eval() >= right.eval()).toBigDecimal()
}

internal class LogicalOrExpr(left: Expr, right: Expr): LogicalOperatorExpr(left, right) {
    override fun eval(): BigDecimal = left.eval() or right.eval()
}

internal class LogicalAndExpr(left: Expr, right: Expr): LogicalOperatorExpr(left, right) {
    override fun eval(): BigDecimal = left.eval() and right.eval()
}

internal class LogicalNotExpr(right: Expr): UnaryExpr(right) {
    override fun eval(): BigDecimal = right.eval().toBool().not().toBigDecimal()
}

@Suppress("MemberVisibilityCanBePrivate")
internal class IfThenElseValueExpr(val cond: Expr, val trueExpr: Expr, val falseExpr: Expr) : Expr() {
    override fun eval() = when {
        cond.eval().toBool() -> trueExpr
        else -> falseExpr
    }.eval()
}

internal abstract class InExpr : Expr()

@Suppress("MemberVisibilityCanBePrivate")
internal class InRangeExpr(val value: Expr, val begin: Expr, val end: Expr) : InExpr() {
    override fun eval(): BigDecimal = (value.eval() in begin.eval()..end.eval()).toBigDecimal()
}

@Suppress("MemberVisibilityCanBePrivate")
internal class InSetExpr(val value: Expr, val elements: List<Expr>) : InExpr() {
    override fun eval(): BigDecimal = value.eval().let { v ->
        elements.any { it.eval() == v }.toBigDecimal()
    }
}

@Suppress("MemberVisibilityCanBePrivate")
internal class NotRangeExpr(val right: InExpr) : InExpr() {
    override fun eval(): BigDecimal = right.eval().toBool().not().toBigDecimal()
}

@Suppress("MemberVisibilityCanBePrivate")
internal class NotInSetExpr(val value: Expr, val elements: List<Expr>) : Expr() {
    override fun eval(): BigDecimal = value.eval().let { v ->
        (!elements.any { it.eval() == v }).toBigDecimal()
    }
}

internal abstract class SymbolExpr<S : Symbol>(val symbol: S) : Expr() {

}

internal class ValueSymbolExpr(symbol: ValueSymbol) : SymbolExpr<ValueSymbol>(symbol), ICanBePercentage {
    override fun eval() = symbol.value

    override val isPercentage = symbol.isPercentage
}

abstract class ArgumentBaseExpr(val param: ParameterSymbol) : Expr()

@Suppress("MemberVisibilityCanBePrivate")
class LazyArgumentExpr(param: ParameterSymbol, val expr: Expr) : ArgumentBaseExpr(param) {
    override fun eval() = expr.eval()
}

@Suppress("MemberVisibilityCanBePrivate")
open class ArgumentExpr(param: ParameterSymbol, val value: BigDecimal) : ArgumentBaseExpr(param) {
    override fun eval() = value
}

class VariadicArgumentExpr(param: ParameterSymbol, val elements: List<Expr>) : ArgumentBaseExpr(param) {
    override fun eval(): BigDecimal = BigDecimal.ZERO
}

class ArgumentZeroExpr(param: ParameterSymbol) : ArgumentExpr(param, BigDecimal.ZERO)

@Suppress("MemberVisibilityCanBePrivate")
internal class FunctionExpr(symbol: FunctionSymbol) : SymbolExpr<FunctionSymbol>(symbol) {
    private val args = mutableListOf<ArgumentBaseExpr>()

    val arguments = FunctionArgumentTable()

    override fun eval(): BigDecimal {
        for (i in 0 until symbol.params.count) {
            arguments[i].expr = args[i]
        }

        return symbol.call(arguments)
    }

    fun addArgument(arg: ArgumentBaseExpr) {
        args += arg
    }

    fun prepare() {
        arguments.clear()
        for (i in 0 until symbol.params.count) {
            val p = symbol.params[i]
            arguments += ArgumentSymbol(p, ArgumentZeroExpr(p))
        }
    }
}

private fun Boolean.toBigDecimal() = if (this) BigDecimal.ONE else BigDecimal.ZERO

private fun BigDecimal.toBool() = this != BigDecimal.ZERO

infix fun BigDecimal.pow(n: BigDecimal): BigDecimal {
    var right = n
    val signOfRight = right.signum()

    right = right.multiply(signOfRight.toBigDecimal())

    val remainderOfRight = right.remainder(BigDecimal.ONE)
    val n2IntPart = right.subtract(remainderOfRight)
    val intPow = pow(n2IntPart.intValueExact(), MathContext.DECIMAL64)
    val doublePow = BigDecimal(toDouble().pow(remainderOfRight.toDouble()))

    var result = intPow.multiply(doublePow, MathContext.DECIMAL64)
    if (signOfRight == -1) result = BigDecimal.ONE.divide(result, MathContext.DECIMAL64.precision, RoundingMode.HALF_UP)

    return result
}

private infix fun BigDecimal.or(with: BigDecimal) = (this.toBool() || with.toBool()).toBigDecimal()

private infix fun BigDecimal.and(with: BigDecimal) = (this.toBool() && with.toBool()).toBigDecimal()
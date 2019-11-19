package com.github.vittee.kformula

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.pow

sealed class Expr {
    abstract fun eval(): BigDecimal

    val subExprCount: Int
        get() = 0

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }
}

internal class NumberExpr(private val value: BigDecimal) : Expr() {
    override fun eval() = value
}

internal abstract class UnaryExpr(val right: Expr) : Expr()

internal abstract class BinaryExpr(val left: Expr, val op: TokenType, val right: Expr) : Expr()

internal abstract class RelationalOperatorExpr(left: Expr, op: TokenType, right: Expr) : BinaryExpr(left, op, right)

internal class NegateExpr(right: Expr) : UnaryExpr(right) {
    override fun eval(): BigDecimal = right.eval().negate()
}

internal class AddExpr(left: Expr, right: Expr): BinaryExpr(left, TokenType.PLUS, right) {
    override fun eval(): BigDecimal = left.eval() + right.eval()
}

internal class SubtractExpr(left: Expr, right: Expr): BinaryExpr(left, TokenType.MINUS, right) {
    override fun eval(): BigDecimal = left.eval() - right.eval()
}

internal class MultiplyExpr(left: Expr, right: Expr): BinaryExpr(left, TokenType.TIMES, right) {
    override fun eval(): BigDecimal = left.eval() * right.eval()
}

internal class DivideExpr(left: Expr, right: Expr): BinaryExpr(left, TokenType.DIVIDE, right) {
    override fun eval(): BigDecimal = left.eval().divide(right.eval(), MathContext.DECIMAL64)
}

internal class ModuloExpr(left: Expr, right: Expr): BinaryExpr(left, TokenType.MOD, right) {
    override fun eval(): BigDecimal = left.eval().remainder(right.eval(), MathContext.DECIMAL64)
}

internal class PowerExpr(left: Expr, right: Expr): BinaryExpr(left, TokenType.EXPONENT, right) {
    override fun eval(): BigDecimal = left.eval() pow right.eval()
}

internal class EqualExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, TokenType.EQUAL, right) {
    override fun eval(): BigDecimal = (left.eval() == right.eval()).toBigDecimal()
}

internal class NotEqualExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, TokenType.EQUAL, right) {
    override fun eval(): BigDecimal = (left.eval() != right.eval()).toBigDecimal()
}

internal class LessExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, TokenType.LESS, right) {
    override fun eval(): BigDecimal = (left.eval() < right.eval()).toBigDecimal()
}

internal class LessEqualExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, TokenType.LESS, right) {
    override fun eval(): BigDecimal = (left.eval() <= right.eval()).toBigDecimal()
}

internal class GreaterExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, TokenType.LESS, right) {
    override fun eval(): BigDecimal = (left.eval() > right.eval()).toBigDecimal()
}

internal class GreaterEqualExpr(left: Expr, right: Expr) : RelationalOperatorExpr(left, TokenType.LESS, right) {
    override fun eval(): BigDecimal = (left.eval() >= right.eval()).toBigDecimal()
}

private fun Boolean.toBigDecimal() = if (this) BigDecimal.ONE else BigDecimal.ZERO

private infix fun BigDecimal.pow(n: BigDecimal): BigDecimal {
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
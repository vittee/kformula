package com.github.vittee.kformula

import java.math.BigDecimal

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

internal class NegateExpr(right: Expr) : UnaryExpr(right) {
    override fun eval(): BigDecimal = right.eval().negate()
}

internal class AddExpr(left: Expr, right: Expr): BinaryExpr(left, TokenType.PLUS, right) {
    override fun eval(): BigDecimal = left.eval().add(right.eval())
}
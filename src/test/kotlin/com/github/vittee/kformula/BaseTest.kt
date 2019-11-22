@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.vittee.kformula

import java.math.BigDecimal
import kotlin.test.expect

abstract class BaseTest {
    protected abstract fun compile(s: String): RootExpr;

    protected fun String.eval() = compile(this).eval()

    protected infix fun String.ee(x: BigDecimal) {
        expect(x) { this.eval() }
    }

    protected infix fun String.ee(x: Int) {
        this ee x.toBigDecimal()
    }

    protected infix fun String.ee(x: Double) {
        this ee x.toBigDecimal()
    }
}
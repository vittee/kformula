package com.github.vittee.kformula

import java.math.BigDecimal

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class BaseFormula {
    protected val symbols = SymbolTable<Symbol>()

    fun addConstant(name: String, value: Int) {
        addConstant(name, value.toBigDecimal())
    }

    fun addConstant(name: String, value: Double) {
        addConstant(name, value.toBigDecimal())
    }

    fun addConstant(name: String, value: BigDecimal) {
        this += ConstValueSymbol(name, value)
    }

    fun addVariable(name: String, value: Int) {
        addVariable(name, value.toBigDecimal())
    }

    fun addVariable(name: String, value: Double) {
        addVariable(name, value.toBigDecimal())
    }

    fun addVariable(name: String, value: BigDecimal) {
        this += DataValueSymbol(name, value)
    }

    fun addExternalVariable(name: String, resolver: ExternalValueResolver) {
        this += ExternalValueSymbol(name, resolver)
    }

    fun addFunction(name: String, vararg signatures: String, handler: FunctionCallHandler) {
        this += FunctionSymbol(name, signatures, handler)
    }

    operator fun plusAssign(symbol: Symbol) {
        symbols += symbol
    }

    fun compile(code: String) = Compiler(symbols).compile(code)

    protected fun addBuiltInConstants() {
        addConstant("PI", Math.PI)
        addConstant("E", Math.E)
    }

    protected fun addBuiltInFunctions() {

    }
}

class Formula : BaseFormula() {
    init {
        addBuiltInConstants()
        addBuiltInFunctions()
    }
}
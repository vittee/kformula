package com.github.vittee.kformula

import java.math.BigDecimal
import java.math.RoundingMode

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
        addAbsFunction()
        addSumFunction()
        addAverageFunction()
        addFloorFunction()
        addCeilFunction()
        addRoundFunction()
        addMinFunction()
        addMaxFunction()
        addClampFunction()
        addSqrtFunction()
    }

    protected fun addSqrtFunction() {
        addFunction("sqrt", "v") { args ->
            args["v"].eval() pow 0.5.toBigDecimal()
        }
    }

    protected fun addClampFunction() {
        addFunction("clamp", "v", "min", "max") { args ->
            args["v"].eval().coerceIn(
                args["min"].eval(),
                args["max"].eval()
            )
        }
    }

    protected fun addMaxFunction() {
        addFunction("max", "...v") { args ->
            args["v"].rest.eval().max()!!
        }
    }

    protected fun addMinFunction() {
        addFunction("min", "...v") { args ->
            args["v"].rest.eval().min()!!
        }
    }

    protected fun addRoundFunction() {
        addFunction("round", "v", "scale=0") { args ->
            args["v"].eval().setScale(
                args["scale"].eval().toInt(),
                RoundingMode.HALF_EVEN
            )
        }
    }

    protected fun addCeilFunction() {
        addFunction("ceil", "v") { args ->
            args["v"].eval().setScale(0, RoundingMode.CEILING)
        }
    }

    protected fun addFloorFunction() {
        addFunction("floor", "v") { args ->
            args["v"].eval().setScale(0, RoundingMode.FLOOR)
        }
    }

    protected fun addAverageFunction() {
        addFunction("average", "...v") { args ->
            args["v"].rest.eval().run {
                reduce { sum, decimal -> sum.add(decimal) } / size.toBigDecimal()
            }
        }
    }

    protected fun addSumFunction() {
        addFunction("sum", "...v") { args ->
            args["v"].rest.eval().reduce { sum, decimal -> sum.add(decimal) }
        }
    }

    protected fun addAbsFunction() {
        addFunction("abs", "v") { args ->
            args["v"].eval().abs()
        }
    }
}

class Formula : BaseFormula() {
    init {
        addBuiltInConstants()
        addBuiltInFunctions()
    }
}
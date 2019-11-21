package com.github.vittee.kformula

import java.math.BigDecimal

sealed class Symbol(name: String) {
    val name = name.toLowerCase()

    override fun equals(other: Any?) = this === other

    override fun hashCode() = System.identityHashCode(this)
}

abstract class ValueSymbol(name: String) : Symbol(name) {
    abstract val value: BigDecimal
}

abstract class DataSymbol(name: String) : ValueSymbol(name) {
    init {
        when (name.first()) {
            '$', '%' -> {}
            else -> throw RuntimeException("Symbol name must begin with $ or %")
        }
    }
}

class ConstValueSymbol(name: String, override val value: BigDecimal) : ValueSymbol(name) {
    constructor(name: String, value: Int) : this(name, value.toBigDecimal())

    constructor(name: String, value: Double) : this(name, value.toBigDecimal())
}

class DataValueSymbol(name: String, override val value: BigDecimal) : DataSymbol(name) {
    constructor(name: String, value: Int) : this(name, value.toBigDecimal())

    constructor(name: String, value: Double) : this(name, value.toBigDecimal())
}

typealias ExternalValueResolver = (String) -> BigDecimal

class ExternalValueSymbol(name: String, private val resolver: ExternalValueResolver) : DataSymbol(name) {
    override val value: BigDecimal
        get() = resolver(name)
}

open class FunctionParameterSymbol(name: String) : Symbol(name) {
    fun isVariadic() = this is FunctionVariadicParameterSymbol
}

class FunctionLazyParameterSymbol(name: String) : FunctionParameterSymbol(name)

class FunctionVariadicParameterSymbol(name: String) : FunctionParameterSymbol(name)

typealias FunctionArgumentTable = SymbolTable<FunctionArgumentSymbol>

typealias FunctionCallHandler = (args: FunctionArgumentTable) -> BigDecimal

@Suppress("MemberVisibilityCanBePrivate")
class FunctionArgumentSymbol(val param: FunctionParameterSymbol, var expr: FunctionArgumentBaseExpr) : Symbol(param.name) {

}

fun FunctionArgumentSymbol?.isVariadic() = this?.param is FunctionVariadicParameterSymbol

fun FunctionArgumentSymbol?.asVariadic() = this?.expr as? FunctionVariadicArgumentExpr

val FunctionArgumentSymbol?.rest
    get() = (this?.expr as? FunctionVariadicArgumentExpr)?.elements

fun List<Expr>?.eval() = this?.map(Expr::eval) ?: emptyList()

fun FunctionArgumentSymbol?.eval() = this?.expr?.eval()

class FunctionSymbol(name: String, signatures: Array<out String>, val handler: FunctionCallHandler) : Symbol(name) {
    val params = SymbolTable<FunctionParameterSymbol>()

    init {
        signatures.forEach { name ->
            val symbol = when {
                name.startsWith("...") -> {
                    FunctionVariadicParameterSymbol(name.drop(3))
                }
                name.startsWith("~") -> {
                    FunctionLazyParameterSymbol(name.drop(1))
                }
                else -> FunctionParameterSymbol(name)
            }

            addParameter(symbol)
        }
    }

    private fun addParameter(symbol: FunctionParameterSymbol) {
        if (params.last() is FunctionVariadicParameterSymbol) {
            throw RuntimeException("Variadic/rest parameter cannot be followed")
        }

        params += symbol
    }
}

@Suppress("MemberVisibilityCanBePrivate", "unused")
open class SymbolTable<S : Symbol> {
    private val container = mutableMapOf<String, S>()

    private val names = mutableListOf<String>()

    val count: Int
        get() = container.size

    fun has(name: String) = container.contains(name.toLowerCase())

    fun has(symbol: S) = container.containsValue(symbol)

    operator fun get(name: String) = container[name.toLowerCase()]

    operator fun get(i: Int) = this[names[i]]

    fun isEmpty() = container.isEmpty()

    fun isNotEmpty() = !isEmpty()

    fun add(symbol: S) {
        if (has(symbol.name)) {
            throw RuntimeException("Symbol ${symbol.name} already exists")
        }

        if (has(symbol)) {
            throw RuntimeException("Symbol already exists")
        }

        container[symbol.name] = symbol
        names += symbol.name
    }

    fun clear() {
        container.clear()
        names.clear()
    }

    fun last() = if (names.isNotEmpty()) container[names.last()] else null

    inline fun <reified T : S> find(name: String): T? = when {
        has(name) && this[name] is T -> {
            this[name] as T
        }
        else -> null
    }

    operator fun plusAssign(symbol: S) {
        add(symbol)
    }
}
package com.github.vittee.kformula

import java.math.BigDecimal

sealed class Symbol(name: String) {
    val name = name.toLowerCase()

    override fun equals(other: Any?) = this === other

    override fun hashCode() = System.identityHashCode(this)
}

abstract class DataSymbol(name: String) : Symbol(name) {
    init {
        when (name.first()) {
            '$', '%' -> {}
            else -> throw RuntimeException("Symbol name must begin with $ or %")
        }
    }

    abstract val value: BigDecimal
}

class ConstDataSymbol(name: String, override val value: BigDecimal) : DataSymbol(name) {
    constructor(name: String, value: Int) : this(name, value.toBigDecimal()) {

    }

    constructor(name: String, value: Double) : this(name, value.toBigDecimal()) {

    }
}

class ExternalDataSymbol(name: String, private val resolver: (String) -> BigDecimal) : DataSymbol(name) {
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


fun FunctionArgumentSymbol?.eval() = this?.expr?.eval()

class FunctionSymbol(name: String, vararg signatures: String, val handler: FunctionCallHandler) : Symbol(name) {
    val params = SymbolTable<FunctionParameterSymbol>()

    init {
        signatures.forEach { name ->
            val symbol = when {
                name.startsWith("...") -> {
                    // TODO: Remove prefix
                    FunctionVariadicParameterSymbol(name)
                }
                name.startsWith("~") -> {
                    // TODO: Remove prefix
                    FunctionLazyParameterSymbol(name)
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
class SymbolTable<S : Symbol> {
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
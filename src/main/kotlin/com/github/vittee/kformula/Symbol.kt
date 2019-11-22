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
    fun eval() = expr.eval()

    val rest
        get() = (expr as? FunctionVariadicArgumentExpr)?.elements ?: throw RuntimeException("Argument is not variadic")
}

fun FunctionArgumentSymbol?.isVariadic() = this?.param is FunctionVariadicParameterSymbol

fun FunctionArgumentSymbol?.asVariadic() = this?.expr as? FunctionVariadicArgumentExpr

fun List<Expr>.eval(): List<BigDecimal> = map(Expr::eval)

class FunctionSymbol(name: String, signatures: Array<out String>, val handler: FunctionCallHandler) : Symbol(name) {
    val params = SymbolTable<FunctionParameterSymbol>()

    init {
        signatures.forEach { s ->
            val signature = parseSignature(s)

            val symbol = when {
                signature.variadic -> {
                    FunctionVariadicParameterSymbol(signature.name)
                }
                signature.lazy -> {
                    FunctionLazyParameterSymbol(signature.name)
                }
                else -> FunctionParameterSymbol(signature.name)
            }

            addParameter(symbol)
        }
    }

    private class SignatureException(s: String) : RuntimeException(s)

    private enum class SignatureState {
        START, NAME, DEFAULT
    }
    
    private data class Signature(val name: String, val default: BigDecimal, val lazy: Boolean, val variadic: Boolean)

    private fun parseSignature(s: String): Signature {
        val len = s.length
        var pos = 0

        var state = SignatureState.START

        fun peek() = if (pos < len) s[pos] else Char.MIN_VALUE
        fun advance() = if (pos < len) s[pos++] else Char.MIN_VALUE

        fun beginName() { state = SignatureState.NAME }
        fun beginDefault() { state = SignatureState.DEFAULT }

        var lazy = false
        var variadic = false
        var name = ""
        var default = ""

        while (pos < len) {
            while (peek() == ' ') {
                advance()
            }

            when (state) {
                SignatureState.START -> {
                    when (peek()) {
                        '.' -> {
                            val dotdotdot = arrayOf(advance(), advance(), advance()).joinToString("")
                            if (dotdotdot != "...") {
                                throw SignatureException("Invalid signature")
                            }

                            variadic = true
                            beginName()
                        }
                        '~' -> {
                            advance()

                            lazy = true
                            beginName()
                        }
                        else -> beginName()
                    }
                }
                SignatureState.NAME -> {
                    when (peek()) {
                        '=' -> {
                            if (variadic) {
                                throw SignatureException("Variadic parameter cannot has default value")
                            }

                            advance()
                            beginDefault()
                        }
                        else -> {
                            name += advance()
                        }
                    }
                }
                SignatureState.DEFAULT -> {
                    default += advance()
                }
            }
        }

        val defaultValue = default.trim().let {
            if (it.isNotEmpty()) try {
                return@let it.toBigDecimal()
            }
            catch(e: Exception) {

            }

            BigDecimal.ZERO
        }
        return Signature(name.trim().toLowerCase(), defaultValue, lazy, variadic)
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

    operator fun get(name: String): S = container[name.toLowerCase()] ?: throw RuntimeException("Symbol $name does not exist")

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
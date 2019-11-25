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

open class ParameterSymbol(name: String, val default: BigDecimal?) : Symbol(name) {
    fun isVariadic() = this is VariadicParameterSymbol
}

class LazyParameterSymbol(name: String, default: BigDecimal?) : ParameterSymbol(name, default)

class VariadicParameterSymbol(name: String) : ParameterSymbol(name, null)

typealias FunctionArgumentTable = SymbolTable<ArgumentSymbol>

typealias FunctionCallHandler = (args: FunctionArgumentTable) -> BigDecimal

@Suppress("MemberVisibilityCanBePrivate")
class ArgumentSymbol(val param: ParameterSymbol, var expr: ArgumentBaseExpr) : Symbol(param.name) {
    fun eval() = expr.eval()

    val rest
        get() = (expr as? VariadicArgumentExpr)?.elements ?: throw RuntimeException("Argument is not variadic")

    operator fun unaryMinus() = -eval()
    operator fun unaryPlus() = eval()
    operator fun not(): BigDecimal = if (eval() == BigDecimal.ZERO) BigDecimal.ONE else BigDecimal.ZERO
    operator fun plus(other: ArgumentSymbol) = eval() + other.eval()
    operator fun minus(other: ArgumentSymbol) = eval() - other.eval()
    operator fun times(other: ArgumentSymbol) = eval() * other.eval()
    operator fun div(other: ArgumentSymbol) = eval() / other.eval()
    operator fun rem(other: ArgumentSymbol) = eval() % other.eval()

    operator fun plus(other: Int) = eval() + other.toBigDecimal()
    operator fun minus(other: Int) = eval() - other.toBigDecimal()
    operator fun times(other: Int) = eval() * other.toBigDecimal()
    operator fun div(other: Int) = eval() / other.toBigDecimal()
    operator fun rem(other: Int) = eval() % other.toBigDecimal()

    operator fun plus(other: Double) = eval() + other.toBigDecimal()
    operator fun minus(other: Double) = eval() - other.toBigDecimal()
    operator fun times(other: Double) = eval() * other.toBigDecimal()
    operator fun div(other: Double) = eval() / other.toBigDecimal()
    operator fun rem(other: Double) = eval() % other.toBigDecimal()

    operator fun plus(other: BigDecimal) = eval() + other
    operator fun minus(other: BigDecimal) = eval() - other
    operator fun times(other: BigDecimal) = eval() * other
    operator fun div(other: BigDecimal) = eval() / other
    operator fun rem(other: BigDecimal) = eval() % other

    operator fun compareTo(other: ArgumentSymbol) = eval().compareTo(other.eval())
    operator fun compareTo(other: Int) = eval().compareTo(other.toBigDecimal())
    operator fun compareTo(other: Double) = eval().compareTo(other.toBigDecimal())
    operator fun compareTo(other: BigDecimal) = eval().compareTo(other)

    operator fun rangeTo(other: ArgumentSymbol) = eval().intValueExact()..other.eval().intValueExact()
    operator fun rangeTo(other: Int) = eval().intValueExact()..other
    operator fun rangeTo(other: BigDecimal) = eval().intValueExact()..other.intValueExact()
}

operator fun Int.plus(other: ArgumentSymbol) = this.toBigDecimal() + other.eval()
operator fun Int.minus(other: ArgumentSymbol) = this.toBigDecimal() - other.eval()
operator fun Int.times(other: ArgumentSymbol) = this.toBigDecimal() * other.eval()
operator fun Int.div(other: ArgumentSymbol) = this.toBigDecimal() / other.eval()
operator fun Int.rem(other: ArgumentSymbol) = this.toBigDecimal() % other.eval()
operator fun Int.compareTo(other: ArgumentSymbol) = this.toBigDecimal().compareTo(other.eval())

operator fun Double.plus(other: ArgumentSymbol) = this.toBigDecimal() + other.eval()
operator fun Double.minus(other: ArgumentSymbol) = this.toBigDecimal() - other.eval()
operator fun Double.times(other: ArgumentSymbol) = this.toBigDecimal() * other.eval()
operator fun Double.div(other: ArgumentSymbol) = this.toBigDecimal() / other.eval()
operator fun Double.rem(other: ArgumentSymbol) = this.toBigDecimal() % other.eval()
operator fun Double.compareTo(other: ArgumentSymbol) = this.toBigDecimal().compareTo(other.eval())

operator fun BigDecimal.plus(other: ArgumentSymbol) = this + other.eval()
operator fun BigDecimal.minus(other: ArgumentSymbol) = this - other.eval()
operator fun BigDecimal.times(other: ArgumentSymbol) = this * other.eval()
operator fun BigDecimal.div(other: ArgumentSymbol) = this / other.eval()
operator fun BigDecimal.rem(other: ArgumentSymbol) = this % other.eval()
operator fun BigDecimal.compareTo(other: ArgumentSymbol) = this.compareTo(other.eval())

operator fun BigDecimal.plus(other: Int) = this + other.toBigDecimal()
operator fun BigDecimal.minus(other: Int) = this - other.toBigDecimal()
operator fun BigDecimal.times(other: Int) = this * other.toBigDecimal()
operator fun BigDecimal.div(other: Int) = this / other.toBigDecimal()
operator fun BigDecimal.rem(other: Int) = this % other.toBigDecimal()

operator fun BigDecimal.plus(other: Double) = this + other.toBigDecimal()
operator fun BigDecimal.minus(other: Double) = this - other.toBigDecimal()
operator fun BigDecimal.times(other: Double) = this * other.toBigDecimal()
operator fun BigDecimal.div(other: Double) = this / other.toBigDecimal()
operator fun BigDecimal.rem(other: Double) = this % other.toBigDecimal()

fun ArgumentSymbol?.isVariadic() = this?.param is VariadicParameterSymbol

fun ArgumentSymbol?.asVariadic() = this?.expr as? VariadicArgumentExpr

fun List<Expr>.eval(): List<BigDecimal> = map(Expr::eval)

class FunctionSymbol(name: String, signatures: Array<out String>, val handler: FunctionCallHandler) : Symbol(name) {
    val params = SymbolTable<ParameterSymbol>()

    init {
        signatures.forEach { s -> parseSignature(s).let { signature ->
            when {
                signature.variadic -> VariadicParameterSymbol(signature.name)
                signature.lazy -> LazyParameterSymbol(signature.name, signature.default)
                else -> ParameterSymbol(signature.name, signature.default)
            }.let(::addParameter)
        }}

        // Variadic should be the last
        for (i in 0 until params.count - 1 ) {
            if (params[i] is VariadicParameterSymbol) {
                throw RuntimeException("Variadic parameter must be in the last position")
            }
        }

        // Default must be tail
        for (i in params.count - 2 downTo 0) {
            val cur = params[i]
            val next = params[i+1]

            if (cur.default != null && next.default == null) {
                throw RuntimeException("Parameter with default value must be tail")
            }
        }
    }

    private class SignatureException(s: String) : RuntimeException(s)

    private enum class SignatureState {
        START, NAME, DEFAULT
    }
    
    private data class Signature(val name: String, val default: BigDecimal?, val lazy: Boolean, val variadic: Boolean)

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

            null
        }
        return Signature(name.trim().toLowerCase(), defaultValue, lazy, variadic)
    }

    private fun addParameter(symbol: ParameterSymbol) {
        if (params.last() is VariadicParameterSymbol) {
            throw RuntimeException("Variadic/rest parameter cannot be followed")
        }

        params += symbol
    }

    fun call(args: FunctionArgumentTable) = handler(args)
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
package com.github.vittee.kformula

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.expect

class CompilerTest : BaseTest() {
    private val compiler = Compiler(SymbolTable<Symbol>().apply {
        this += ConstValueSymbol("CONST1", 12345)
        this += DataValueSymbol("%fifty", 0.5)
        this += DataValueSymbol("\$2pi", Math.PI*2)
        this += ExternalValueSymbol("\$external") { (Math.PI*3).toBigDecimal() }

        this += FunctionSymbol("one", emptyArray()) {
            BigDecimal.ONE
        }

        this += FunctionSymbol("identity", arrayOf("v")) { args ->
            args["v"].eval()
        }

        this += FunctionSymbol("add", arrayOf("a", "b=1")) { args ->
            args["a"].eval() + args["b"].eval()
        }

        this += FunctionSymbol("accumulate", arrayOf("init", "...all")) { args ->
            args[0].eval() + args["all"].rest.eval().reduce { sum, decimal -> sum.add(decimal) }
        }
    })

    override fun compile(s: String) = compiler.compile(s)

    @Test
    fun `Empty source should fail`() {
        assertFails { compiler.compile("") }
    }

    @Test
    fun `Literal expressions`() {
        compile("1")
        "-0" ee 0
        "1" ee 1
        "1_000" ee 1000
        "1.234" ee 1.234
        "50%" ee 0.5
        "true" ee 1
        "false" ee 0
    }

    @Test
    fun `()`() {
        "(1)" ee 1
        "((1))" ee 1
    }

    @Test
    fun `Empty expression within () should fail`() {
        assertFails {
            "()".eval()
        }
    }

    @Test
    fun `Multiple express is not supported`() {
        assertFails {
            "1 2".eval()
        }

        assertFails {
            "(1 2)".eval()
        }
    }

    @Test
    fun `Unary operators`() {
        "+1" ee 1
        "-1" ee -1
        "++1" ee 1
        "--1" ee 1
        "-+1" ee -1
        "+-1" ee -1
        "-(1)" ee -1
    }

    @Test
    fun `Not operator`() {
        "not true" ee 0
        "not false" ee 1
        "not 0" ee 1
        "not 1" ee 0
        "not 2" ee 0
        "not not true" ee 1
        "not(not true)" ee 1
    }

    @Test
    fun `! operator`() {
        "!true" ee 0
        "!false" ee 1
        "!0" ee 1
        "!1" ee 0
        "!2" ee 0
        "!!true" ee 1
        "!(!true)" ee 1
    }

    @Test
    fun `Multiplication operator`() {
        "1*1" ee 1
        "3*1" ee 3
        "1*3" ee 3
        "1*0" ee 0
        "1*2*3" ee 6
        "1.5*2" ee 3
        "3/2" ee 1.5
    }

    @Test
    fun `Dividing operator`() {
        "1/1" ee 1
        "6/3/2" ee 1
    }

    @Test
    fun `Modulo operator`() {
        "10 mod 3" ee 1
        "10 mod 2" ee 0
        "0 mod 1" ee 0
        "10 mod 3.3" ee 0.1
    }

    @Test
    fun `Exponent operator`() {
        "1000^0" ee 1
        "2^8" ee 256
        "9^0.5" ee 3
        "9^(1/2)" ee 3
    }

    @Test
    fun `Addition operator`() {
        "1+0" ee 1
        "1+1" ee 2
        "1+-1" ee 0
    }

    @Test
    fun `Subtract operator`() {
        "1-0" ee 1
        "1-1" ee 0
        "1-+1" ee 0
        "1--1" ee 2
    }

    @Test
    fun `Logical OR`() {
        "false or false" ee 0
        "true or false" ee 1
        "false or true" ee 1
        "true or true" ee 1
        "false or not true" ee 0
        "not true or true" ee 1
    }

    @Test
    fun `Logical AND`() {
        "true and false" ee 0
        "true and true" ee 1
        "not true and true" ee 0
    }

    @Test
    fun `Equal operators`() {
        "1=2" ee 0
        "1=1" ee 1
        "1==2" ee 0
        "1==1" ee 1
        "2=1+1" ee 1
        "1+1=2" ee 1
    }

    @Test
    fun `Not equal operators`() {
        "1!=2" ee 1
        "1!=1" ee 0
        "1<>2" ee 1
        "1<>1" ee 0
        "2!=1+1" ee 0
        "1+1!=2" ee 0
    }

    @Test
    fun `Relational operators`() {
        "1>0" ee 1
        "1>=1" ee 1
        "0>1" ee 0
        "0<1" ee 1
        "1<=1" ee 1
    }

    @Test
    fun `If Then Else`() {
        "if 1>2 then 90 else 0" ee 0
        "if 1>2 then 90" ee 0
        "if 2>1 then 90 else 0 " ee 90
        "if 2>1 then 90 " ee 90
        "if 2>1 90 else 0 " ee 90
        "if 2>1 90 " ee 90
    }

    @Test
    fun `IF function`() {
        "IF(1>2,90,0)" ee 0
        "IF(1>2,90)" ee 0
        "IF(2>1,90,0)" ee 90
    }

    @Test
    fun `IN BETWEEN`() {
        "1 in 1 between 10" ee 1
        "0 in 1 between 10" ee 0
        "1 in 1..10" ee 1
        "0 in 1..10" ee 0
    }

    @Test
    fun `NOT IN BETWEEN`() {
        "1 not in 1 between 10" ee 0
        "0 not in 1 between 10" ee 1
        "1 not in 1..10" ee 0
        "0 not in 1..10" ee 1
    }

    @Test
    fun `!IN BETWEEN`() {
        "1 !in 1 between 10" ee 0
        "0 !in 1 between 10" ee 1
        "1 !in 1..10" ee 0
        "0 !in 1..10" ee 1
    }

    @Test
    fun `Constant expression`() {
        "CONST1" ee 12345
        "CONST1-CONST1" ee 0
    }

    @Test
    fun `Variable expressions`() {
        "%fifty*100" ee 50
        "\$2pi" ee Math.PI*2
        "\$2pi^2" ee Math.PI*Math.PI*2*2
        "\$external" ee Math.PI*3
    }

    @Test
    fun `Simple function`() {
        "one()" ee 1
        "one()+1" ee 2
        "one()+one()+one()" ee 3
        assertFails { "one(0)".eval() }
    }

    @Test
    fun `Single parameter function`() {
        "identity(0)" ee 0
        "identity(one())" ee 1
        assertFails { "identity()".eval() }
    }

    @Test
    fun `Default parameter`() {
        "add(one())" ee 2
        "add(one(), 0)" ee 1
        "add(one(), 0.5)" ee 1.5
    }

    @Test
    fun `Variadic parameter`() {
        "accumulate(20, one())" ee 21
        "accumulate(20, one()*2, 3, 5)" ee 30
        "accumulate(accumulate(20, -9, -8, -3), 9, 8, 7)" ee 24
    }

    @Test
    fun `Safe eval`() {
        assertFails { compile("1/0").eval() }
        expect(BigDecimal.ZERO) { compile("1/0").safeEval() }
    }
}
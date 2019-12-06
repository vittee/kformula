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
        this += DataValueSymbol("\$record.value", 99)
        this += DataValueSymbol("\$ตัวแปร", 999)
        this += DataValueSymbol("\$変数", 9999)
        this += ExternalValueSymbol("\$external") { (Math.PI*3).toBigDecimal() }

        this += FunctionSymbol("one", emptyArray()) {
            BigDecimal.ONE
        }

        this += FunctionSymbol("identity", arrayOf("v")) { args ->
            args["v"].eval()
        }

        this += FunctionSymbol("add", arrayOf("a", "b=1")) { args ->
            args["a"] + args["b"]
        }

        this += FunctionSymbol("accumulate", arrayOf("init", "...all")) { args ->
            val all = args["all"].rest.eval()
            args["init"] + all.reduce { sum, v -> sum.add(v) }
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
    fun `IN RANGE`() {
        "1 in 1..10" ee 1
        "0 in 1..10" ee 0
    }

    @Test
    fun `NOT IN RANGE`() {
        "1 not in 1..10" ee 0
        "0 not in 1..10" ee 1
    }

    @Test
    fun `!IN RANGE`() {
        "1 !in 1..10" ee 0
        "0 !in 1..10" ee 1
    }

    @Test
    fun `IN set`() {
        "2 in [1,2,3,4]" ee 1
        "9 in [1,2,3,4]" ee 0
    }

    @Test
    fun `NOT IN set`() {
        "2 not in [1,2,3,4]" ee 0
        "9 not in [1,2,3,4]" ee 1
    }

    @Test
    fun `!IN set`() {
        "2 !in [1,2,3,4]" ee 0
        "9 !in [1,2,3,4]" ee 1
    }

    @Test
    fun `Constant expression`() {
        "CONST1" ee 12345
        "CONST1-CONST1" ee 0
    }

    @Test
    fun `Variable expressions`() {
        "%fifty" ee 0.5
        "\$2pi" ee Math.PI*2
        "\$2pi^2" ee Math.PI*Math.PI*2*2
        "\$external" ee Math.PI*3
        "\$record.value" ee 99
        "\$ตัวแปร" ee 999
        "\$変数" ee 9999
    }

    @Test
    fun `Percentage operations with number`() {
        "50% + 1" ee 1.5
        "%fifty + 1" ee 1.5
        "100% - 0.5" ee 0.5
        "50% * 2" ee 1
        "%fifty * 2" ee 1
        "50% / 5" ee 0.1
        "%fifty / 5" ee 0.1
        "100% ^ 3" ee 1
        "200% ^ 3" ee 8
    }

    @Test
    fun `Percentage operations with percentage`() {
        "50% + 50%" ee 1
        "%fifty + %fifty" ee 1
        "100% - 20%" ee 0.8
        "50% * 100%" ee 0.5
        "%fifty * 100%" ee 0.5
        "50% * 50%" ee 0.25
        "%fifty * %fifty" ee 0.25
        "50% / 50%" ee 1
        "%fifty / %fifty" ee 1
        "50% ^ 100%" ee 0.5
        "%fifty ^ 100%" ee 0.5
    }

    @Test
    fun `Number operations with percentage`() {
        "100 + 20%" ee 120
        "200 + %fifty" ee 300
        "100 - 20%" ee 80
        "300 - %fifty" ee 150
        "500 * 20%" ee 100
        "500 * %fifty" ee 250
        "500 / 20%" ee 2500
        "500 / %fifty" ee 1000
        "81 ^ 50%" ee 9
        "81 ^ %fifty" ee 9
    }

    @Test
    fun `Left hand side percentage operations`() {
        "50% + 50% + 1" ee 2
        "50% - 50% + 1" ee 1
        "(50% + 50%) * 2" ee 2
        "(50% + 50%) / 2" ee 0.5
        "(50% * 3) * 2 ^ 3" ee 27
        "(%fifty * 3) * 2 ^ 3" ee 27
    }

    @Test
    fun `Right hand side percentage operations`() {
        "100 + (50% + 50%)" ee 200
        "100 + (50% + 2)" ee 350
        "100 + (50% - 1)" ee 50
        "60 + (100% * 2)" ee 180
        "60 + (300% / 3)" ee 120
        "100 - (50% + 50%)" ee 0
        "60 - (50% + 2)" ee -90
        "60 - (%fifty + 2)" ee -90
        "100 * (50% + 30%)" ee 80
        "24 / (50% + 30%)" ee 30
        "(60 + 50%) + 50%" ee 135
        "(60 + %fifty) + 50%" ee 135
        "(60 - 50%) + 50%" ee 45
        "(60 - %fifty) + 50%" ee 45
        "100 * 40% + 50%" ee 60
        "60 * 40% + 50%" ee 36
        "400 / 50% + 50%" ee 1200
    }

    @Test
    fun `Right recursive percentage operations`() {
        "100 + 50% + 100%" ee 300
        "60 + 50% + 50%" ee 135
        "60 - 50% + 50%" ee 45
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
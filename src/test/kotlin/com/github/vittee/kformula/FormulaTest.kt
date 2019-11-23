package com.github.vittee.kformula

import kotlin.test.Test
import kotlin.test.assertFails

class FormulaTest : BaseTest() {
    private val fx = Formula().apply {
        addVariable("\$price", 1690)
        addVariable("\$discounted", 1014)
        addVariable("%discount", 0.40)
    }

    override fun compile(s: String) = fx.compile(s)

    @Test
    fun `Pre-defined constants`() {
        "PI" ee Math.PI
        "E" ee Math.E
    }

    @Test
    fun `Absolute function`() {
        "abs(-10)" ee 10
        "abs(-true)" ee 1
    }

    @Test
    fun `Min Max Clamp`() {
        "min(5,1,0,-9,20,4)" ee -9
        "max(5,1,0,-9,20,4)" ee 20
        "clamp(33, 20, 25)" ee 25
        "clamp(10, 20, 25)" ee 20
        "clamp(22, 20, 25)" ee 22
    }

    @Test
    fun `Sum function`() {
        assertFails { "sum()".eval() }
        "sum(1,2,3,4,5,6,7,8,9)" ee 45
    }

    @Test
    fun `Average function`() {
        assertFails { "average()".eval() }
        "average(1,2,3,4,5,6,7,8,9)" ee 5
    }

    @Test
    fun `Rounding functions`() {
        "floor(3.14)" ee 3
        "ceil(3.14)" ee 4
        "round(3.14)" ee 3
        "round(3.56)" ee 4
        "round(3.56,1)" ee 3.6
        "round(3.567,2)" ee 3.57
    }

    @Test
    fun `Percentage calculation`() {
        "\$price * %discount" ee 676
        "\$price * -%discount" ee -676
        "\$price - (\$price * %discount)" ee 1014
        "\$price * (1-%discount)" ee 1014
        "\$discounted / (1-%discount)" ee 1690
        "add_percentage(\$price, +50%)" ee 2535
        "add_percentage(\$price, -%discount)" ee 1014
        "subtract_percentage(\$price, %discount)" ee 1014
    }
}
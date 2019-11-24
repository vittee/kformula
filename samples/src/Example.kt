import com.github.vittee.kformula.*
import java.math.BigDecimal

fun main() {
    val fx = Formula().apply {
        addVariable("\$test", 300.5)
        addVariable("%discount", 0.5)
        addExternalVariable("\$ext") {
            99.99.toBigDecimal()
        }

        addFunction("one") {
            println("one() was called")
            BigDecimal.ONE
        }

        addFunction("rand", "min=1", "max=2000") { args ->
            println("rand() was called")
            (args["min"]..args["max"]).random().toBigDecimal()
        }

        addFunction("myFunc", "...all") { args ->
            println("myFunc() was called")
            val all = args["all"].rest.eval()
            println("all=$all")

            BigDecimal.ONE
        }

        addFunction("add", "a", "b=1") { args ->
            println("add() was called")
            args["a"] + args["b"]
        }

        this += FunctionSymbol("accumulate", arrayOf("init", "...all")) { args ->
            val all = args["all"].rest.eval()
            args["init"] + all.reduce { sum, v -> sum.add(v) }
        }
    }

    val program = fx.compile("myFunc(9, \$ext, add(rand(), 1%))")

    println(program.eval().toPlainString())
}
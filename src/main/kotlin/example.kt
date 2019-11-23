import com.github.vittee.kformula.*
import java.math.BigDecimal

fun main() {
    val fx = Formula().apply {
        addVariable("\$test", 300.5)
        addVariable("%discount", 0.5)
        addExternalVariable("\$ext") {
            99.99.toBigDecimal()
        }

        addFunction("rand") {
            println("rand called")
            (1..2000).random().toBigDecimal()
        }

        addFunction("myFunc", "...c") { args ->
            println("myFunc called")
            val all = args["c"].rest.eval()
            println("all is  $all")
//            val t = args["t"].eval()
//            println("t is $t")

            BigDecimal.ONE
        }

        addFunction("add", "a", "b=1") { args ->
            args["a"] + args["b"]
        }
    }

    val program = fx.compile("subtract_percentage(\$test,%discount)")

    println(program.eval().toPlainString())
}
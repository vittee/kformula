import com.github.vittee.kformula.*
import java.math.BigDecimal

fun main() {
    val fx = Formula().apply {
        addVariable("\$test", 55.555)
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
            args["a"].eval() + args["b"].eval()
        }
    }

    val program = fx.compile("add(9)")

    println(program.eval().toPlainString())
}
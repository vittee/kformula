import com.github.vittee.kformula.Compiler

fun main() {
    val compiler = Compiler()
    val program = compiler.compile("(not)")

    println(program.eval().toPlainString())
}
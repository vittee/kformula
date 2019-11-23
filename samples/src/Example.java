import com.github.vittee.kformula.Formula;

import java.math.BigDecimal;

public class Example {
    public static void main(String[] args) {
        run();
    }

    private static void run() {
        final Formula fx = new Formula();

        fx.addVariable("$test", 300.5);
        fx.addVariable("%discount", 0.5);
        fx.addExternalVariable("$ext", s -> BigDecimal.valueOf(99.99));

        fx.addFunction("rand", new String[]{}, args -> BigDecimal.valueOf(Math.random() * 100));

        fx.addFunction("add", new String[]{"a", "b=1"}, args -> {
            BigDecimal a = args.get("a").eval();
            BigDecimal b = args.get("b").eval();

            return a.add(b);
        });

        System.out.println(
                fx.compile("add($ext,1%)")
                        .eval()
                        .toPlainString()
        );
    }
}

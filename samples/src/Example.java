import com.github.vittee.kformula.Expr;
import com.github.vittee.kformula.Formula;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Example {
    private static Random r = new Random();


    public static void main(String[] args) {
        run();
    }

    private static void run() {
        final Formula fx = new Formula();

        fx.addVariable("$test", 300.5);
        fx.addVariable("%discount", 0.5);
        fx.addExternalVariable("$ext", s -> BigDecimal.valueOf(99.99));

        fx.addFunction("rand", new String[]{}, args -> {
            System.out.println("rand() was called");
            return BigDecimal.valueOf(r.nextInt(2000) + 1);
        });

        fx.addFunction("myFunc", new String[]{"...all"}, args -> {
            System.out.println("myFunc() was called");

            List<Expr> exprs = args.get("all").getRest();
            List<BigDecimal> all = exprs.stream().map(Expr::eval).collect(Collectors.toList());

            System.out.println("all="+all.toString());

            return BigDecimal.ONE;
        });

        fx.addFunction("add", new String[]{"a", "b=1"}, args -> {
            BigDecimal a = args.get("a").eval();
            BigDecimal b = args.get("b").eval();

            return a.add(b);
        });

        System.out.println(
                fx.compile("myFunc(9, $ext, add(rand(), 1%))")
                        .eval()
                        .toPlainString()
        );
    }
}

package astava.parse2;

import java.util.function.Supplier;

public class Parsers {
    public static <T, Success, Failure> Parser<T, Success, Failure> ref(Supplier<Parser<T, Success, Failure>> supplier) {
        return (ctx, source) -> supplier.get().parse(ctx, source);
    }

    public static <T, Success> Parser<T, Success, String> atEnd() {
        return (ctx, source) -> {
            if(source.atEnd())
                return ctx.success(source, null);
            else
                return ctx.failure(source, "Expected at end.");
        };
    }
}

package astava.tree;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Util {
    public static <T> T returnFrom(Consumer<Consumer<T>> body, Supplier<T> defaultBody) {
        boolean[] didReturnHolder = new boolean[1];
        T[] valueHolder = (T[])new Object[1];
        body.accept(value -> {
            valueHolder[0] = value;
            didReturnHolder[0] = true;
        });
        return didReturnHolder[0] ? valueHolder[0] : defaultBody.get();
    }

    public static <T> T returnFrom(T defaultValue, Consumer<Consumer<T>> body) {
        T[] valueHolder = (T[])new Object[1];
        valueHolder[0] = defaultValue;
        body.accept(value -> valueHolder[0] = value);
        return valueHolder[0];
    }
}

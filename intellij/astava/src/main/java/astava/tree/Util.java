package astava.tree;

import java.util.function.Consumer;

public class Util {
    public static <T> T returnFrom(T defaultValue, Consumer<Consumer<T>> body) {
        T[] valueHolder = (T[])new Object[1];
        valueHolder[0] = defaultValue;
        body.accept(value -> valueHolder[0] = value);
        return valueHolder[0];
    }
}

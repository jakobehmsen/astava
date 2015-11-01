package astava.tree;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static <T extends Dom> T map(T dom, BiFunction<Function<T, T>, T, T> mapper) {
        return mapper.apply(new Function<T, T>() {
            @Override
            public T apply(T d) {
                List<Dom> newChildren = d.getChildren().stream().map(c -> mapper.apply(this, (T)c)).collect(Collectors.toList());
                return (T)dom.setChildren(newChildren);
            }
        }, dom);
    }
}

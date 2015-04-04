package astava.samples.virela.view;

import java.util.function.Consumer;

public interface CellConsumer<T> {
    void next(T value, Consumer<CellConsumer> remain);
    default void atEnd() { }

    public static abstract class Infinite<T> implements CellConsumer<T> {
        @Override
        public void next(T value, Consumer<CellConsumer> remain) {
            next(value);
            remain.accept(this);
        }

        public abstract void next(T value);
    }
}

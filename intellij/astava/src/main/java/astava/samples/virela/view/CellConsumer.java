package astava.samples.virela.view;

public interface CellConsumer<T> {
    void next(T value);
    default void atEnd() { }
}

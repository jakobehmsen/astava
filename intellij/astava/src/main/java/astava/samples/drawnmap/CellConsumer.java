package astava.samples.drawnmap;

public interface CellConsumer<T> {
    void next(T value);
    default void atEnd() { }
}

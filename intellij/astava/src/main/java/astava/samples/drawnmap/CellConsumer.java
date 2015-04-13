package astava.samples.drawnmap;

public interface CellConsumer<T> {
    default void setBinding(Binding binding) { }
    void next(T value);
    default void atEnd() { }
}

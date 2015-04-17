package astava.samples.drawnmap;

public interface Cell<T> {
    Binding consume(CellConsumer<T> consumer);/* {
        addConsumer(consumer);
        return () -> removeConsumer(consumer);
    }
    void addConsumer(CellConsumer<T> consumer);
    void removeConsumer(CellConsumer<T> consumer);*/
    default void moveConsumersTo(Cell cell) { }
    T value();

    default void acceptBinding(Binding x) { }
}

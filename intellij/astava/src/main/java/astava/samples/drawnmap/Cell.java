package astava.samples.drawnmap;

public interface Cell<T> {
    Binding consume(CellConsumer<T> consumer);
}

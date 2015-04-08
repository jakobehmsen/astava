package astava.samples.virela.view;

public interface Cell<T> {
    Binding consume(CellConsumer<T> consumer);
}

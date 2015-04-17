package astava.samples.drawnmap;

public interface CellConsumer<T> {
    default void setDescription(Object description) { }
    default Object getDescription() { return null; }

    /*default void setSource(Cell<T> source) { }
    default Cell<T> getSource() { return null; }*/

    default void setBinding(Binding binding) { }
    default Binding getBinding() { return null; }

    void next(T value);
    default void atEnd() { }
}

package astava.samples.drawnmap;

public class Singleton<T> implements Cell<T> {
    private T value;

    public Singleton(T value) {
        this.value = value;
    }

    @Override
    public Binding consume(CellConsumer<T> consumer) {
        consumer.next(value);

        return () -> { };
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public T value() {
        return value;
    }
}

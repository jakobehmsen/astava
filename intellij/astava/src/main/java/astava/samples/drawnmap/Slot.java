package astava.samples.drawnmap;

import java.util.ArrayList;

public class Slot<T> implements Cell<T> {
    private T currentValue;
    private ArrayList<CellConsumer<T>> consumers = new ArrayList<>();

    public void set(T value) {
        setCurrentValue(value);
    }

    private void setCurrentValue(T currentValue) {
        this.currentValue = currentValue;
        consumers.forEach(c -> c.next(currentValue));
    }

    public void beDeleted() {
        consumers.forEach(c -> c.atEnd());
        consumers = null;
    }

    @Override
    public Binding consume(CellConsumer<T> consumer) {
        if(currentValue != null)
            consumer.next(currentValue);

        consumers.add(consumer);

        return () -> consumers.remove(consumer);
    }
}

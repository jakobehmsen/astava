package astava.samples.virela.view;

import java.util.ArrayList;

public class Slot<T> implements Cell<T> {
    private Cell value;
    private T currentValue;
    private ArrayList<CellConsumer<T>> consumers = new ArrayList<>();
    private Binding currentBinding;

    public void set(Cell<T> value) {
        if(currentBinding != null)
            currentBinding.remove();
        this.value = value;
        currentBinding = value.consume(v -> setCurrentValue(v));
    }

    private void setCurrentValue(T currentValue) {
        this.currentValue = currentValue;
        consumers.forEach(c -> c.next(currentValue));
    }

    public void beDeleted() {
        currentBinding.remove();
        consumers.forEach(c -> c.atEnd());
        value = null;
        consumers = null;
        currentBinding = null;
    }

    @Override
    public Binding consume(CellConsumer<T> consumer) {
        if(currentValue != null)
            consumer.next(currentValue);

        consumers.add(consumer);

        return () -> consumers.remove(consumer);
    }
}

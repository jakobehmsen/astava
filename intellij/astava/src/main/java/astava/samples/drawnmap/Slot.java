package astava.samples.drawnmap;

import java.util.ArrayList;

public class Slot<T> implements Cell<T>, CellConsumer<T> {
    private T currentValue;
    private ArrayList<CellConsumer<T>> consumers = new ArrayList<>();

    public void set(T value) {
        setCurrentValue(value);
    }

    private void setCurrentValue(T currentValue) {
        this.currentValue = currentValue;
        consumers.forEach(c -> c.next(currentValue));
    }

    @Override
    public Binding consume(CellConsumer<T> consumer) {
        consumer.next(value(null));
        consumers.add(consumer);

        return () ->
            consumers.remove(consumer);
    }

    public void beDeleted() {
        consumers.forEach(c -> c.atEnd());
        consumers = null;
    }

    private Binding binding;

    @Override
    public void setBinding(Binding binding) {
        if(this.binding != null)
            this.binding.remove();
        this.binding = binding;
    }

    @Override
    public Binding getBinding() {
        return binding;
    }

    @Override
    public void next(T value) {
        set(value);
    }

    private Object description;

    @Override
    public void setDescription(Object description) {
        this.description = description;
    }

    @Override
    public Object getDescription() {
        return description;
    }

    @Override
    public T value(Object[] args) {
        return currentValue;
    }
}

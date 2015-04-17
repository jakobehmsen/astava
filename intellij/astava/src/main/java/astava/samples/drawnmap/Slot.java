package astava.samples.drawnmap;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Slot<T> implements Cell<T>, CellConsumer<T> {
    private T currentValue;
    private ArrayList<MovableBinding> consumers = new ArrayList<>();

    public void set(T value) {
        setCurrentValue(value);
    }

    private void setCurrentValue(T currentValue) {
        this.currentValue = currentValue;
        consumers.forEach(c -> c.consumer.next(currentValue));
    }

    private static class MovableBinding implements Binding {
        private Slot owner;

        public MovableBinding(Slot owner, CellConsumer consumer) {
            this.owner = owner;
            this.consumer = consumer;
        }

        private CellConsumer consumer;

        public void moveTo(Slot newOwner) {
            newOwner.consume(consumer);
            owner = newOwner;
        }

        @Override
        public void remove() {
            owner.consumers.remove(consumer);
        }
    }

    @Override
    public Binding consume(CellConsumer<T> consumer) {
        MovableBinding binding = new MovableBinding(this, consumer);
        consumer.next(value());
        consumers.add(binding);
        return binding;
    }

    public void beDeleted() {
        consumers.forEach(c -> c.consumer.atEnd());
        consumers = null;
    }

    /*@Override
    public void addConsumer(CellConsumer<T> consumer) {
        if(currentValue != null)
            consumer.next(currentValue);

        consumers.add(consumer);
    }

    @Override
    public void removeConsumer(CellConsumer<T> consumer) {
        consumers.remove(consumer);
    }*/

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
    public T value() {
        return currentValue;
    }

    @Override
    public void moveConsumersTo(Cell cell) {
        consumers.forEach(x -> cell.acceptBinding(x));
    }

    @Override
    public void acceptBinding(Binding x) {
        consumers.add((MovableBinding)x);
        ((MovableBinding) x).consumer.next(value());
    }
}

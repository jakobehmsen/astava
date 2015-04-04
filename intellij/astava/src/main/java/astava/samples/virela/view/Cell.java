package astava.samples.virela.view;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

public abstract class Cell<T> {
    private JComponent view;

    protected Cell(JComponent view) {
        this.view = view;
    }

    public JComponent getView() {
        return view;
    }

    public abstract void consume(CellConsumer<T> consumer);

    /*public void beRemoved() {

    }

    public void addObserver(Consumer<T> o) {
        observers.add(o);
    }

    public void removeObserver(Consumer<T> o) {
        observers.remove(o);
    }

    public void propogate(T value) {
        observers.forEach(o -> o.accept(value));
    }*/
}

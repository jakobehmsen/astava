package astava.samples.virela.view;

import javax.swing.*;

public abstract class Cell<T> {
    private JComponent view;

    protected Cell(JComponent view) {
        this.view = view;
    }

    public JComponent getView() {
        return view;
    }

    public abstract void consume(CellConsumer<T> consumer);
}

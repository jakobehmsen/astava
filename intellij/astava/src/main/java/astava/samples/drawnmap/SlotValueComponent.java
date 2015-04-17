package astava.samples.drawnmap;

import javax.swing.*;

public interface SlotValueComponent {
    public interface Listener {
        void valueChanged(Object value);
    }

    JComponent getComponent();
    boolean accepts(Object value);
    void setValue(Object value);
}

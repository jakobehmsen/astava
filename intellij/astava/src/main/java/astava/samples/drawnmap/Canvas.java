package astava.samples.drawnmap;

import javax.swing.*;

public interface Canvas {
    void beginSelect();
    void endSelect();
    JComponent findComponent(int x, int y);
    boolean isSelected(JComponent component);
    void select(String variableName, JComponent component);
    void deselect(JComponent component);
    void setScript(String src);
    void clearSelection();
}

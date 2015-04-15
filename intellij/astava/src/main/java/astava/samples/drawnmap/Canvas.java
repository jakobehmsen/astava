package astava.samples.drawnmap;

import javax.swing.*;

public interface Canvas {
    // Content is at 0
    // Selection is at 1
    // Overlay at 2
    // Selection must persist after overlay

    void beginSelect();
    void endSelect();
    JComponent findComponent(int x, int y);
    boolean isSelected(JComponent component);
    JComponent getSelected(String id);
    void select(String variableName, JComponent component);
    void deselect(JComponent component);
    void setScript(String src);
    void clearSelection();
}

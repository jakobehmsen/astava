package astava.samples.drawnmap;

import javax.swing.*;

public interface Tool {
    String getText();
    default void activate() { }
    default void deactivate() { }
    ToolSession startSession(int x, int y);
    void setTarget(JComponent target);
}

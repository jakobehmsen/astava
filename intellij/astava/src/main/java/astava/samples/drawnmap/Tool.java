package astava.samples.drawnmap;

import javax.swing.*;

public interface Tool {
    String getText();
    ToolSession startSession(JComponent target, int x, int y);
}

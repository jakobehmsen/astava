package astava.samples.drawnmap;

import javax.swing.*;

public class MarkTool implements Tool {
    @Override
    public String getText() {
        return "Mark";
    }

    @Override
    public ToolSession startSession(JComponent target, int x, int y) {
        return null;
    }
}

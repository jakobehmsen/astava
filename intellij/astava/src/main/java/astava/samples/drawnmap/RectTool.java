package astava.samples.drawnmap;

import javax.swing.*;

public class RectTool implements Tool {
    @Override
    public String getText() {
        return "Rect";
    }

    @Override
    public ToolSession startSession(JComponent target, int x, int y) {
        return null;
    }
}

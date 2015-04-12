package astava.samples.drawnmap;

import javax.swing.*;

public abstract class AbstractTool implements Tool {
    private String text;
    private JComponent target;

    protected AbstractTool(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setTarget(JComponent target) {
        this.target = target;
    }

    public JComponent getTarget() {
        return target;
    }
}

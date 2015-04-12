package astava.samples.drawnmap;

import javax.swing.*;

public abstract class AbstractTool implements Tool {
    private String text;
    private JLayeredPane target;

    protected AbstractTool(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setTarget(JLayeredPane target) {
        this.target = target;
    }

    public JLayeredPane getTarget() {
        return target;
    }
}

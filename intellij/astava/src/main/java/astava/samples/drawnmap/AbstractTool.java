package astava.samples.drawnmap;

import javax.swing.*;

public abstract class AbstractTool implements Tool {
    private String text;
    private JLayeredPane target;
    private Canvas canvas;

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

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}

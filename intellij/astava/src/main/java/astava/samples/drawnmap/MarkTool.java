package astava.samples.drawnmap;

import javax.swing.*;

public class MarkTool extends AbstractTool {
    public MarkTool() {
        super("Mark");
    }

    @Override
    public void activate() {
        canvas.beginSelect();
    }

    @Override
    public void deactivate() {
        canvas.endSelect();
    }

    private Canvas canvas;

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public ToolSession startSession(int x, int y) {
        JComponent componentOver = canvas.findComponent(x, y);

        if(componentOver != null) {
            if(canvas.isSelected(componentOver)) {
                canvas.deselect(componentOver);
            } else {
                canvas.select(null, componentOver);
            }
        }

        return NullToolSession.INSTANCE;
    }
}

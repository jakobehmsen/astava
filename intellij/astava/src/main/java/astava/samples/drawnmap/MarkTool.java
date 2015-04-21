package astava.samples.drawnmap;

import javax.swing.*;

public class MarkTool extends AbstractTool {
    public MarkTool() {
        super("Mark");
    }

    @Override
    public void activate() {
        getCanvas().beginSelect();
    }

    @Override
    public void deactivate() {
        getCanvas().endSelect();
    }

    @Override
    public ToolSession startSession(int x, int y) {
        JComponent componentOver = getCanvas().findComponent(x, y);

        if(componentOver != null) {
            if(getCanvas().isSelected(componentOver)) {
                getCanvas().deselect(componentOver);
            } else {
                getCanvas().select(null, componentOver);
            }
        }

        return NullToolSession.INSTANCE;
    }
}

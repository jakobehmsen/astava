package astava.samples.drawnmap;

import javax.swing.*;

public class InspectTool extends AbstractTool {
    public InspectTool() {
        super("Inspect");
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
            canvas.clearSelection();

            Description description = (Description)((CellConsumer<?>) componentOver).getDescription();

            description.getIdToCellMap().entrySet().forEach(e -> {
                canvas.select(e.getKey(), (JComponent)e.getValue());
                canvas.setScript(description.getSrc());
            });
        }

        return NullToolSession.INSTANCE;
    }
}

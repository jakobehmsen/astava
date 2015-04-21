package astava.samples.drawnmap;

import javax.swing.*;

public class InspectTool extends AbstractTool {
    public InspectTool() {
        super("Inspect");
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
            getCanvas().clearSelection();

            Description description = (Description)((CellConsumer<?>) componentOver).getDescription();

            description.getIdToCellMap().entrySet().forEach(e -> {
                getCanvas().select(e.getKey(), (JComponent) e.getValue());
                getCanvas().setScript(description.getSrc());
            });
        }

        return NullToolSession.INSTANCE;
    }
}

package astava.samples.drawnmap;

import javax.swing.*;
import java.util.Map;

public interface Tool {
    String getText();
    default void activate() { }
    default void deactivate() { }
    ToolSession startSession(int x, int y);
    void setTarget(JLayeredPane target);
    default void setEnvironment(Map<String, Cell> environment) { }
    default void setCanvas(Canvas canvas) { }
}

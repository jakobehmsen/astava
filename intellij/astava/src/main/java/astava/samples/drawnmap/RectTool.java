package astava.samples.drawnmap;

import javax.swing.*;
import java.awt.*;

public class RectTool extends AbstractTool {
    public RectTool() {
        super("Rect");
    }

    private static class Rect extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    @Override
    public ToolSession startSession(int x1, int y1) {
        Rect rect = new Rect();

        rect.setLocation(x1, y1);

        getTarget().add(rect);

        return new ToolSession() {
            @Override
            public void update(int x2, int y2) {
                int left = Math.min(x1, x2);
                int right = Math.max(x1, x2);
                int top = Math.min(y1, y2);
                int bottom = Math.max(y1, y2);
                int xDelta = right - left;
                int yDelta = bottom - top;

                int xDir = x1 < x2
                    ? 0 // LeftRight
                    : 1 ; // RightLeft
                int yDir = y1 < y2
                    ? 0 // TopDown
                    : 1 ; // BottomUp

                rect.setSize(xDelta + 1, yDelta + 1);
                rect.setLocation(left, top);

                rect.revalidate();
                rect.repaint();
            }

            @Override
            public void end() {

            }
        };
    }
}

package astava.samples.drawnmap;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class RectTool implements Tool {
    @Override
    public String getText() {
        return "Rect";
    }

    private static class Rect extends JComponent{
        private int x1;
        private int y1;
        private int x2;
        private int y2;

        private Rect(int x1, int y1) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x1;
            this.y2 = y1;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2D = (Graphics2D)g;

            int left = Math.min(x1, x2);
            int right = Math.max(x1, x2);
            int top = Math.min(y1, y2);
            int bottom = Math.max(y1, y2);
            int xDelta = right - left;
            int yDelta = bottom - top;

            Rectangle2D rec = new Rectangle2D.Float(left, top, xDelta, yDelta);
            g2D.draw(rec);
        }
    }

    @Override
    public ToolSession startSession(JComponent target, int x, int y) {
        Rect rect = new Rect(x, y);

        rect.setSize(target.getSize());

        target.add(rect);
        target.revalidate();
        target.repaint();

        return new ToolSession() {
            @Override
            public void update(int x, int y) {
                rect.x2 = x;
                rect.y2 = y;
                rect.revalidate();
                rect.repaint();
            }

            @Override
            public void end() {
                int left = Math.min(rect.x1, rect.x2);
                int right = Math.max(rect.x1, rect.x2);
                int top = Math.min(rect.y1, rect.y2);
                int bottom = Math.max(rect.y1, rect.y2);
                int xDelta = right - left;
                int yDelta = bottom - top;

                int xDir = rect.x1 < rect.x2
                    ? 0 // LeftRight
                    : 1 ; // RightLeft
                int yDir = rect.y1 < rect.y2
                    ? 0 // TopDown
                    : 1 ; // BottomUp

                rect.setSize(xDelta + 1, yDelta + 1);
                rect.setLocation(left, top);
                rect.x1 = xDir == 0 ? 0 : xDelta;
                rect.y1 = yDir == 0 ? 0 : yDelta;
                rect.x2 = xDir == 1 ? 0 : xDelta;
                rect.y2 = yDir == 1 ? 0 : yDelta;
                rect.revalidate();
                rect.repaint();
            }
        };
    }
}

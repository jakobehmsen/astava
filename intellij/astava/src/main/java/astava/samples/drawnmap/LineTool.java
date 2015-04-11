package astava.samples.drawnmap;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

public class LineTool implements Tool {
    @Override
    public String getText() {
        return "Line";
    }

    private static class Line extends JComponent{
        private int x1;
        private int y1;
        private int x2;
        private int y2;

        private Line(int x1, int y1) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x1;
            this.y2 = y1;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2D = (Graphics2D)g;
            Line2D lin = new Line2D.Float(x1, y1, x2, y2);
            g2D.draw(lin);
            System.out.println(x1 + "," + y1 + "," + x2 + "," + y2);
        }
    }

    @Override
    public ToolSession startSession(JComponent target, int x, int y) {
        Line line = new Line(x, y);

        line.setSize(target.getSize());

        target.add(line);
        target.revalidate();
        target.repaint();

        return new ToolSession() {
            @Override
            public void update(int x, int y) {
                line.x2 = x;
                line.y2 = y;
                line.revalidate();
                line.repaint();
            }

            @Override
            public void end() {
                int left = Math.min(line.x1, line.x2);
                int right = Math.max(line.x1, line.x2);
                int top = Math.min(line.y1, line.y2);
                int bottom = Math.max(line.y1, line.y2);
                int xDelta = right - left;
                int yDelta = bottom - top;

                int xDir = line.x1 < line.x2
                    ? 0 // LeftRight
                    : 1 ; // RightLeft
                int yDir = line.y1 < line.y2
                    ? 0 // TopDown
                    : 1 ; // BottomUp

                line.setSize(xDelta + 1, yDelta + 1);
                line.setLocation(left, top);
                line.x1 = xDir == 0 ? 0 : xDelta;
                line.y1 = yDir == 0 ? 0 : yDelta;
                line.x2 = xDir == 1 ? 0 : xDelta;
                line.y2 = yDir == 1 ? 0 : yDelta;
                line.revalidate();
                line.repaint();
            }
        };
    }
}

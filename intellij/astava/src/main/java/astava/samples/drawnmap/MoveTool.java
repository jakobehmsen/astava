package astava.samples.drawnmap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MoveTool extends AbstractTool {
    public MoveTool() {
        super("Move");
    }

    private JPanel overlay;

    private static final int NA = -1;
    private static final int FIRST = 0;
    private static final int MIDDLE = 1;
    private static final int LAST = 2;
    private static final int BORDER_SIZE = 5;

    private int getPosition(int position, int start, int end) {
        if(within(position, start, end)) {
            if(within(position, start, start + BORDER_SIZE))
                return FIRST;
            else if(within(position, end - BORDER_SIZE, end))
                return LAST;
            else
                return MIDDLE;
        }

        return NA;
    }

    private boolean within(int position, int start, int end) {
        return start < position && position < end;
    }

    private int cursorType(int hPosition, int vPosition) {
        switch (hPosition) {
            case FIRST:
                switch (vPosition) {
                    case FIRST:
                        return Cursor.NW_RESIZE_CURSOR;
                    case MIDDLE:
                        return Cursor.W_RESIZE_CURSOR;
                    case LAST:
                        return Cursor.SW_RESIZE_CURSOR;
                }
            case MIDDLE:
                switch (vPosition) {
                    case FIRST:
                        return Cursor.N_RESIZE_CURSOR;
                    case MIDDLE:
                        return Cursor.CROSSHAIR_CURSOR;
                    case LAST:
                        return Cursor.S_RESIZE_CURSOR;
                }
            case LAST:
                switch (vPosition) {
                    case FIRST:
                        return Cursor.NE_RESIZE_CURSOR;
                    case MIDDLE:
                        return Cursor.E_RESIZE_CURSOR;
                    case LAST:
                        return Cursor.SE_RESIZE_CURSOR;
                }
        }

        return Cursor.DEFAULT_CURSOR;
    }

    int hPos = NA;
    int vPos = NA;

    @Override
    public void activate() {
        overlay = new JPanel();
        overlay.setSize(getTarget().getSize());
        overlay.setOpaque(false);
        getTarget().add(overlay);
        getTarget().setLayer(overlay, JLayeredPane.DRAG_LAYER + 100);
        Cursor cursor = getTarget().getCursor();
        MouseAdapter mouseAdapter = new MouseAdapter() {
            ToolSession session;

            @Override
            public void mousePressed(MouseEvent e) {
                session = startSession(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                session.end();
                session = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                session.update(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                JComponent component = getCanvas().findComponent(e.getX(), e.getY());

                if (component != null) {
                    int hPosNew = getPosition(e.getX(), component.getX(), component.getX() + component.getWidth());
                    int vPosNew = getPosition(e.getY(), component.getY(), component.getY() + component.getHeight());

                    if(hPos != hPosNew || vPos != vPosNew) {
                        hPos = hPosNew;
                        vPos = vPosNew;

                        int cursorType = cursorType(hPos, vPos);

                        getTarget().setCursor(Cursor.getPredefinedCursor(cursorType));
                    }
                } else {
                    hPos = NA;
                    vPos = NA;

                    getTarget().setCursor(cursor);
                }
            }
        };
        overlay.addMouseListener(mouseAdapter);
        overlay.addMouseMotionListener(mouseAdapter);
    }

    @Override
    public void deactivate() {
        getTarget().remove(overlay);
    }

    @Override
    public ToolSession startSession(int x, int y) {
        JComponent component = getCanvas().findComponent(x, y);

        if(component == null)
            return null;

        int x1 = component.getX();
        int y1 = component.getY();
        int xDelta = x - x1;
        int yDelta = y - y1;
        int width1 = component.getWidth();
        int height1 = component.getHeight();
        int right1 = x1 + width1;
        int bottom1 = y1 + height1;

        return new ToolSession() {
            @Override
            public void update(int x2, int y2) {
                int x = component.getX();
                int y = component.getY();
                int width = component.getWidth();
                int height = component.getHeight();

                switch (hPos) {
                    case FIRST:
                        x = x2;
                        width = right1 - x;
                        break;
                    case LAST:
                        width = x2 - x1;
                        break;
                }

                switch (vPos) {
                    case FIRST:
                        y = y2;
                        height = bottom1 - y;
                        break;
                    case LAST:
                        height = y2 - y1;
                        break;
                }

                if(hPos == MIDDLE && vPos == MIDDLE) {
                    x = x2 - xDelta;
                    y = y2 - yDelta;
                }

                component.setLocation(x, y);
                component.setSize(width, height);

                component.revalidate();
                component.repaint();
            }

            @Override
            public void end() {

            }
        };
    }
}

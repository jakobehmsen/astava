package astava.samples.drawnmap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MarkTool extends AbstractTool {
    public MarkTool() {
        super("Mark");
    }

    private JPanel overlay;

    @Override
    public void activate() {
        overlay = new JPanel();
        overlay.setSize(getTarget().getSize());
        overlay.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(MouseListener l: getTarget().getMouseListeners())
                    l.mouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                for(MouseListener l: getTarget().getMouseListeners())
                    l.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for(MouseListener l: getTarget().getMouseListeners())
                    l.mouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                for(MouseListener l: getTarget().getMouseListeners())
                    l.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                for(MouseListener l: getTarget().getMouseListeners())
                    l.mouseExited(e);
            }
        });
        overlay.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                for(MouseMotionListener l: getTarget().getMouseMotionListeners())
                    l.mouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                for(MouseMotionListener l: getTarget().getMouseMotionListeners())
                    l.mouseMoved(e);
            }
        });
        overlay.setOpaque(false);

        getTarget().add(overlay);
        getTarget().setLayer(overlay, JLayeredPane.DRAG_LAYER);
    }

    @Override
    public void deactivate() {
        selections.forEach(m -> getTarget().remove(m.marking));
        selections.forEach(m -> environment.remove(m.variableName));
        selections.clear();
        getTarget().remove(overlay);
        getTarget().revalidate();
        getTarget().repaint();
    }

    private Map<String, Cell> environment;

    @Override
    public void setEnvironment(Map<String, Cell> environment) {
        this.environment = environment;
    }

    private static class Selection {
        private final JComponent componentOver;
        private final JComponent marking;
        private final String variableName;

        private Selection(JComponent componentOver, JComponent marking, String variableName) {
            this.componentOver = componentOver;
            this.marking = marking;
            this.variableName = variableName;
        }
    }

    private ArrayList<Selection> selections = new ArrayList<>();

    @Override
    public ToolSession startSession(int x, int y) {
        JComponent componentOver = (JComponent)Arrays.asList(getTarget().getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)).stream().filter(c ->
            c.getBounds().contains(x, y)).findFirst().orElseGet(() -> null);

        if(componentOver != null && componentOver != getTarget() && !selections.stream().anyMatch(m -> m.componentOver == componentOver)) {
            JPanel marking = new JPanel(new BorderLayout());
            marking.setBackground(Color.RED);
            String variableName = nextVariableName();
            JLabel variableNameLabel = new JLabel(variableName);
            variableNameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD | Font.ITALIC, 16));
            variableNameLabel.setOpaque(true);

            variableNameLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(0, 2, 2, 2)
            ));
            marking.add(variableNameLabel, BorderLayout.NORTH);
            marking.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(Color.DARK_GRAY, 1.0f, 2.0f, 2.0f, false),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 1.0f, 2.0f, 2.0f, false),
                    BorderFactory.createDashedBorder(Color.DARK_GRAY, 1.0f, 2.0f, 2.0f, false)
                )
            ));
            marking.setOpaque(false);

            int sizeExtra = 6;
            int topExtra = 20;//variableNameLabel.getFont().getLineMetrics(variableName, ) variableNameLabel.getHeight();
            marking.setSize(sizeExtra + componentOver.getWidth() + sizeExtra, topExtra + componentOver.getHeight() + sizeExtra);
            marking.setLocation(componentOver.getX() - sizeExtra, componentOver.getY() - topExtra);

            getTarget().add(marking, JLayeredPane.DRAG_LAYER);
            getTarget().revalidate();
            getTarget().repaint();
            selections.add(new Selection(componentOver, marking, variableName));

            environment.put(variableName, (Cell)componentOver);
        }

        return NullToolSession.INSTANCE;
    }

    private java.util.List<Character> getSeed() {
        return IntStream.range('a', 'z').mapToObj(x -> Character.valueOf((char)x)).collect(Collectors.toList());
    }

    private String nextVariableName() {
        int chIndex = selections.size() % getSeed().size();
        char ch = getSeed().get(chIndex);

        return "" + ch;
    }
}

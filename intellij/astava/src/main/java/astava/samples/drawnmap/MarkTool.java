package astava.samples.drawnmap;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
        markings.forEach(m -> getTarget().remove(m));
        markings.clear();
        getTarget().remove(overlay);
        getTarget().revalidate();
        getTarget().repaint();
        selectedComponents.clear();
    }

    private ArrayList<JComponent> markings = new ArrayList<>();
    private HashSet<JComponent> selectedComponents = new HashSet<>();

    @Override
    public ToolSession startSession(int x, int y) {
        JComponent componentOver = (JComponent)Arrays.asList(getTarget().getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)).stream().filter(c ->
            c.getBounds().contains(x, y)).findFirst().orElseGet(() -> null);

        if(componentOver != null && componentOver != getTarget() && !selectedComponents.contains(componentOver)) {
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
                BorderFactory.createDashedBorder(Color.DARK_GRAY, 2.0f, 2.0f, 2.0f, false),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 1.0f, 2.0f, 2.0f, false),
                    BorderFactory.createDashedBorder(Color.DARK_GRAY, 2.0f, 2.0f, 2.0f, false)
                )
            ));
            marking.setOpaque(false);

            int sizeExtra = 18;
            marking.setSize(componentOver.getWidth() + sizeExtra, componentOver.getHeight() + sizeExtra);
            marking.setLocation(componentOver.getX() - sizeExtra / 2, componentOver.getY() - sizeExtra / 2);

            getTarget().add(marking, JLayeredPane.DRAG_LAYER);
            getTarget().revalidate();
            getTarget().repaint();
            markings.add(marking);
            selectedComponents.add(componentOver);
        }

        return NullToolSession.INSTANCE;
    }

    private java.util.List<Character> getSeed() {
        return IntStream.range('a', 'z').mapToObj(x -> Character.valueOf((char)x)).collect(Collectors.toList());
    }

    private String nextVariableName() {
        int chIndex = selectedComponents.size() % getSeed().size();
        char ch = getSeed().get(chIndex);

        return "" + ch;
    }
}

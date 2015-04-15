package astava.samples.drawnmap;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InspectTool extends AbstractTool {
    public InspectTool() {
        super("Inspect");
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

        seedIndex = 0;
    }

    @Override
    public void deactivate() {
        if(descriptionView != null)
            getTarget().remove(descriptionView);
        selections.forEach(m -> getTarget().remove(m.marking));
        selections.forEach(m -> environment.remove(m.variableName));
        selections.clear();
        getTarget().remove(overlay);
        getTarget().revalidate();
        getTarget().repaint();
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

    private JTextPane descriptionView;
    private ArrayList<Selection> selections = new ArrayList<>();

    private Map<String, Cell> environment;

    @Override
    public void setEnvironment(Map<String, Cell> environment) {
        this.environment = environment;
    }

    @Override
    public ToolSession startSession(int x, int y) {
        JComponent componentOver = (JComponent) Arrays.asList(getTarget().getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)).stream().filter(c ->
            c.getBounds().contains(x, y)).findFirst().orElseGet(() -> null);

        if(componentOver != null && componentOver != getTarget() && componentOver != descriptionView) {
            if(descriptionView != null)
                getTarget().remove(descriptionView);
            selections.forEach(m -> getTarget().remove(m.marking));
            selections.forEach(m -> environment.remove(m.variableName));
            selections.clear();

            Description description = (Description)((CellConsumer<?>) componentOver).getDescription();

            if(description != null) {
                description.getIdToCellMap().entrySet().forEach(e -> {
                    JPanel marking = new JPanel(new BorderLayout());
                    marking.setBackground(Color.RED);
                    String variableName = e.getKey();
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

                    int sizeExtra = 2;
                    int topExtra = 24;//variableNameLabel.getFont().getLineMetrics(variableName, ) variableNameLabel.getHeight();
                    marking.setSize(sizeExtra + ((JComponent) e.getValue()).getWidth() + sizeExtra, topExtra + ((JComponent) e.getValue()).getHeight() + sizeExtra);
                    marking.setLocation(((JComponent) e.getValue()).getX() - sizeExtra, ((JComponent) e.getValue()).getY() - topExtra);

                    getTarget().add(marking, JLayeredPane.DRAG_LAYER);
                    selections.add(new Selection((JComponent) e.getValue(), marking, variableName));

                    if(e.getValue() == componentOver) {
                        JLabel descriptionView = new JLabel();
                        descriptionView.setText(description.getSrc());
                        //descriptionView.setLocation(marking.getX(), marking.getY() + marking.getHeight());
                        //descriptionView.setSize(marking.getWidth(), 30);
                        descriptionView.setFont(new Font(Font.MONOSPACED, Font.BOLD | Font.ITALIC, 16));
                        descriptionView.setOpaque(true);
                        marking.add(descriptionView, BorderLayout.SOUTH);
                        marking.setSize(marking.getWidth(), marking.getHeight() + topExtra);
                        //getTarget().add(descriptionView);
                    }

                    environment.put(variableName, (Cell) e.getValue());
                });
            }

            getTarget().revalidate();
            getTarget().repaint();
        }

        return NullToolSession.INSTANCE;
    }

    private java.util.List<Character> getSeed() {
        return IntStream.range('a', 'z' + 1).mapToObj(x -> Character.valueOf((char)x)).collect(Collectors.toList());
    }

    private int seedIndex;

    private String nextVariableName() {
        int chIndex = seedIndex % getSeed().size();
        char ch = getSeed().get(chIndex);
        String name = "" + ch;

        for(int i = 0; i < seedIndex / getSeed().size(); i++)
            name += ch;

        seedIndex++;

        return name;
    }
}

package astava.samples.drawnmap;

import astava.samples.drawnmap.lang.antlr4.DrawNMapBaseVisitor;
import astava.samples.drawnmap.lang.antlr4.DrawNMapLexer;
import astava.samples.drawnmap.lang.antlr4.DrawNMapParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainView extends JFrame implements Canvas {
    private java.util.List<Tool> tools;
    private JComponent toolBoxView;
    private JLayeredPane canvasView;
    private JComponent scriptView;
    private Hashtable<String, Cell> environment = new Hashtable<>();



    private static class Selection {
        private final JComponent component;
        private final JComponent marking;
        private final String variableName;

        private Selection(JComponent componentOver, JComponent marking, String variableName) {
            this.component = componentOver;
            this.marking = marking;
            this.variableName = variableName;
        }
    }

    private ArrayList<Selection> selections = new ArrayList<>();

    private JPanel overlay;

    @Override
    public void beginSelect() {
        overlay = new JPanel();
        overlay.setSize(canvasView.getSize());
        overlay.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(MouseListener l: canvasView.getMouseListeners())
                    l.mouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                for(MouseListener l: canvasView.getMouseListeners())
                    l.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for(MouseListener l: canvasView.getMouseListeners())
                    l.mouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                for(MouseListener l: canvasView.getMouseListeners())
                    l.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                for(MouseListener l: canvasView.getMouseListeners())
                    l.mouseExited(e);
            }
        });
        overlay.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                for(MouseMotionListener l: canvasView.getMouseMotionListeners())
                    l.mouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                for(MouseMotionListener l: canvasView.getMouseMotionListeners())
                    l.mouseMoved(e);
            }
        });
        overlay.setOpaque(false);

        canvasView.add(overlay);
        canvasView.setLayer(overlay, JLayeredPane.DRAG_LAYER);

        seedIndex = 0;
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

    @Override
    public void endSelect() {
        canvasView.remove(overlay);
        overlay = null;
    }

    @Override
    public JComponent findComponent(int x, int y) {
        JComponent componentOver = (JComponent)Arrays.asList(canvasView.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)).stream().filter(c ->
            c.getBounds().contains(x, y)).findFirst().orElseGet(() -> null);

        return componentOver;
    }

    @Override
    public boolean isSelected(JComponent component) {
        return selections.stream().anyMatch(x -> x.component == component);
    }

    @Override
    public JComponent getSelected(String id) {
        return selections.stream().filter(x -> x.variableName.equals(id)).map(x -> x.component).findFirst().orElseGet(() -> null);
    }

    @Override
    public void select(String variableName, JComponent component) {
        JPanel marking = new JPanel(new BorderLayout());
        marking.setBackground(Color.RED);
        if(variableName == null)
            variableName = nextVariableName();
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
        marking.setSize(sizeExtra + component.getWidth() + sizeExtra, topExtra + component.getHeight() + sizeExtra);
        marking.setLocation(component.getX() - sizeExtra, component.getY() - topExtra);

        canvasView.add(marking, JLayeredPane.DRAG_LAYER);
        canvasView.revalidate();
        canvasView.repaint();
        selections.add(new Selection(component, marking, variableName));

        environment.put(variableName, (Cell) component);
    }

    @Override
    public void deselect(JComponent component) {
        Selection selection = selections.stream().filter(s -> s.component == component).findFirst().orElseGet(() -> null);

        selections.remove(selection);
        if(selections.isEmpty())
            seedIndex = 0;

        canvasView.remove(selection.marking);
        environment.remove(selection.variableName);
        canvasView.revalidate();
        canvasView.repaint();
    }

    @Override
    public void clearSelection() {
        selections.forEach(s -> {
            canvasView.remove(s.marking);
            environment.remove(s.variableName);
        });
        selections.clear();
        canvasView.revalidate();
        canvasView.repaint();
    }

    private static class Selector {
        private final String name;
        private final Class<?>[] parameterTypes;

        private Selector(String name, Class<?>[] parameterTypes) {
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public int hashCode() {
            return name.hashCode() * Arrays.hashCode(parameterTypes);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Selector) {
                Selector objSelector = (Selector)obj;
                return this.name.equals(objSelector.name) &&
                    Arrays.equals(this.parameterTypes, objSelector.parameterTypes);
            }

            return false;
        }
    }

    private Hashtable<Selector, Function<Object[], Object>> functions = new Hashtable<>();

    private Function<Object[], Object> resolve(String name, Class<?>[] parameterTypes) {
        return functions.get(new Selector(name, parameterTypes));
    }

    private void define(String name, Class<?>[] parameterTypes, Function<Object[], Object> function) {
        functions.put(new Selector(name, parameterTypes), function);
    }

    private <Return> void define(String name, Supplier<Return> function) {
        define(name, new Class<?>[0], args -> function.get());
    }

    private <P0, Return> void define(String name, Class<P0> param1, Function<P0, Return> function) {
        define(name, new Class<?>[]{param1}, args -> function.apply((P0) args[0]));
    }

    private <P0, P1, Return> void define(String name, Class<P0> param1, Class<P1> param2, BiFunction<P0, P1, Return> function) {
        define(name, new Class<?>[]{param1, param2}, args -> function.apply((P0)args[0], (P1)args[1]));
    }

    public MainView(java.util.List<Tool> tools) {
        this.tools = tools;

        setTitle("Draw'n'map");

        toolBoxView = createToolBoxView();
        canvasView = createCanvasView();
        scriptView = createScriptView();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBoxView, BorderLayout.NORTH);
        getContentPane().add(canvasView, BorderLayout.CENTER);
        getContentPane().add(scriptView, BorderLayout.SOUTH);

        define("+", BigDecimal.class, BigDecimal.class, (lhs, rhs) -> lhs.add(rhs));
        define("-", BigDecimal.class, BigDecimal.class, (lhs, rhs) -> lhs.subtract(rhs));
        define("/", BigDecimal.class, BigDecimal.class, (lhs, rhs) -> lhs.divide(rhs));
        define("*", BigDecimal.class, BigDecimal.class, (lhs, rhs) -> lhs.multiply(rhs));

        define("+", String.class, String.class, (lhs, rhs) -> lhs.concat(rhs));
    }

    private ButtonGroup toolBoxButtonGroup;

    private JComponent createToolBoxView() {
        JToolBar toolBar = new JToolBar();

        toolBar.setFloatable(false);

        toolBoxButtonGroup = new ButtonGroup();

        for(int i = 0; i < tools.size(); i++) {
            Tool tool = tools.get(i);

            JRadioButton b = new JRadioButton();
            b.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    tool.activate();
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    tool.deactivate();
                }
            });
            b.setActionCommand("" + i);
            if(i == 0) {
                b.setSelected(true);
            }
            b.setText(tool.getText());
            toolBoxButtonGroup.add(b);
            toolBar.add(b);
        }

        return toolBar;
    }

    private Tool getSelectedTool() {
        int indexOfTool = Integer.parseInt(toolBoxButtonGroup.getSelection().getActionCommand());
        return tools.get(indexOfTool);
    }

    private MouseAdapter canvasMouseAdapterProxy = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            canvasMouseAdapter.mouseClicked(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            canvasMouseAdapter.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            canvasMouseAdapter.mouseReleased(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            canvasMouseAdapter.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            canvasMouseAdapter.mouseExited(e);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            canvasMouseAdapter.mouseWheelMoved(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            canvasMouseAdapter.mouseDragged(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            canvasMouseAdapter.mouseMoved(e);
        }
    };
    private MouseAdapter canvasMouseAdapter;

    private JLayeredPane createCanvasView() {
        JLayeredPane view = new JLayeredPane();

        view.setLayout(null);

        switchCanvasMousePending();

        view.addMouseListener(canvasMouseAdapterProxy);
        view.addMouseMotionListener(canvasMouseAdapterProxy);

        tools.forEach(t -> t.setTarget(view));

        return view;
    }

    private void switchCanvasMousePending() {
        canvasMouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Tool tool = getSelectedTool();
                switchCanvasMouseAction(tool.startSession(e.getX(), e.getY()));
            }
        };
    }

    private void switchCanvasMouseAction(ToolSession toolSession) {
        canvasMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
               toolSession.update(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                toolSession.end();
                switchCanvasMousePending();
            }
        };
    }

    private Border createScriptViewBorder(Color color) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, color),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
    }

    private JComponent createScriptView() {
        JTextPane view = new JTextPane();

        view.setBorder(createScriptViewBorder(Color.BLACK));
        view.setFont(new Font(Font.MONOSPACED, Font.BOLD | Font.ITALIC, 16));

        view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String sourceCode = view.getText();

                    try {
                        ANTLRInputStream in = new ANTLRInputStream(new ByteArrayInputStream(sourceCode.getBytes()));
                        DrawNMapLexer lexer = new DrawNMapLexer(in);
                        DrawNMapParser parser = new DrawNMapParser(new CommonTokenStream(lexer));

                        DrawNMapParser.ProgramContext programCtx = parser.program();

                        Border b = view.getBorder();

                        if (parser.getNumberOfSyntaxErrors() == 0) {
                            evaluateProgram(programCtx);

                            view.setBorder(createScriptViewBorder(Color.GREEN));
                        } else {
                            view.setBorder(createScriptViewBorder(Color.RED));
                        }

                        Timer timer = new Timer(500, e1 -> view.setBorder(b));
                        timer.setRepeats(false);
                        timer.start();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        tools.forEach(t -> t.setEnvironment(environment));
        tools.forEach(t -> t.setCanvas(this));

        return view;
    }

    @Override
    public void setScript(String src) {
        ((JTextPane)scriptView).setText(src);
    }

    private int nextOutX = 30;
    private int nextOutY = 30;

    private void updateOuts(int width, int height) {
        nextOutY += height + 30;
    }

    private void evaluateProgram(DrawNMapParser.ProgramContext programCtx) {
        programCtx.accept(new DrawNMapBaseVisitor<Void>() {
            @Override
            public Void visitAssign(@NotNull DrawNMapParser.AssignContext ctx) {
                Map<String, Cell> idToCellMap = new Hashtable<>();

                String variableName = ctx.ID().getText();
                CellConsumer<Object> target = (CellConsumer<Object>) environment.get(variableName);

                Cell<Object> source = (Cell<Object>) reduceSource(ctx.expression(), idToCellMap);

                String srcCode = ctx.getText();

                if (target == null) {
                    // Undeclared element; implies request for allocation of new element
                    // Make new element eagerly from right hand side

                    Object value = source.value();

                    JComponent newElement = null;

                    if(value instanceof BigDecimal) {
                        newElement = new NumberTool.Number();
                        newElement.setSize(60, 20);
                    } else if(value instanceof String) {
                        newElement = new TextTool.Text();
                        newElement.setSize(60, 20);
                    }

                    idToCellMap.put(variableName, (Cell)newElement);

                    Binding binding = source.consume((CellConsumer<Object>)newElement);
                    ((CellConsumer<Object>)newElement).setBinding(binding);

                    ((CellConsumer<Object>)newElement).setDescription(new Description(idToCellMap, srcCode));

                    newElement.setLocation(nextOutX, nextOutY);

                    updateOuts(newElement.getWidth(), newElement.getHeight());

                    canvasView.add(newElement);

                    select(variableName, newElement);
                } else {
                    idToCellMap.put(variableName, (Cell)target);

                    Binding binding = source.consume(target);
                    target.setBinding(binding);

                    target.setDescription(new Description(idToCellMap, srcCode));
                }

                return null;
            }
        });
    }

    private Cell<Object> createBinaryOperation(String operator, Cell<Object> lhsCell, Cell<Object> rhsCell) {
        return new Cell<Object>() {
            @Override
            public Binding consume(CellConsumer<Object> consumer) {
                return new Binding() {
                    private Object lhsValue;
                    private Object rhsValue;

                    private Binding lhsBinding = lhsCell.consume(next -> {
                        lhsValue = next;
                        update();
                    });

                    private Binding rhsBinding = rhsCell.consume(next -> {
                        rhsValue = next;
                        update();
                    });

                    private void update() {
                        if(lhsValue != null && rhsValue != null) {
                            Object next = reduce(operator, lhsValue, rhsValue);
                            consumer.next(next);
                        }
                    }

                    @Override
                    public void remove() {
                        lhsBinding.remove();
                        rhsBinding.remove();
                    }
                };
            }

            @Override
            public Object value() {
                return reduce(operator, lhsCell.value(), rhsCell.value());
            }
        };
    }

    private Object reduce(String operator, Object lhs, Object rhs) {
        Function<Object[], Object> function = functions.get(new Selector(operator, new Class<?>[]{lhs.getClass(), rhs.getClass()}));

        if(function != null)
            return function.apply(new Object[]{lhs, rhs});

        return null;
    }

    private Cell<?> reduceSource(ParserRuleContext ctx, Map<String, Cell> idToCellMap) {
        return ctx.accept(new DrawNMapBaseVisitor<Cell>() {
            @Override
            public Cell visitAddExpression(@NotNull DrawNMapParser.AddExpressionContext ctx) {
                Cell lhs = reduceSource(ctx.mulExpression(0), idToCellMap);

                if(ctx.mulExpression().size() > 1) {
                    for(int i = 1; i < ctx.mulExpression().size(); i++) {
                        Cell<Object> rhsCell = (Cell<Object>)reduceSource(ctx.mulExpression(i), idToCellMap);

                        Cell<Object> lhsCell = (Cell<Object>)lhs;

                        String operator = ctx.ADD_OP(i - 1).getText();

                        lhs = createBinaryOperation(operator, lhsCell, rhsCell);
                    }
                }

                return lhs;
            }

            @Override
            public Cell visitMulExpression(@NotNull DrawNMapParser.MulExpressionContext ctx) {
                Cell lhs = reduceSource(ctx.leafExpression(0), idToCellMap);

                if(ctx.leafExpression().size() > 1) {
                    for(int i = 1; i < ctx.leafExpression().size(); i++) {
                        Cell<Object> rhsCell = (Cell<Object>)reduceSource(ctx.leafExpression(i), idToCellMap);

                        Cell<Object> lhsCell = (Cell<Object>)lhs;

                        String operator = ctx.MUL_OP(i - 1).getText();

                        lhs = createBinaryOperation(operator, lhsCell, rhsCell);
                    }
                }

                return lhs;
            }

            @Override
            public Cell visitFunctionCall(@NotNull DrawNMapParser.FunctionCallContext ctx) {
                return super.visitFunctionCall(ctx);
            }

            @Override
            public Cell visitId(@NotNull DrawNMapParser.IdContext ctx) {
                String id = ctx.ID().getText();
                Cell cell = environment.get(id);

                idToCellMap.put(id, cell);

                return cell;
            }

            @Override
            public Cell visitNumber(@NotNull DrawNMapParser.NumberContext ctx) {
                return new Singleton<>(new BigDecimal(ctx.NUMBER().getText()));
            }

            @Override
            public Cell visitString(@NotNull DrawNMapParser.StringContext ctx) {
                String value = ctx.STRING().getText().substring(1, ctx.STRING().getText().length() - 1);
                return new Singleton<>(value);
            }

            @Override
            public Cell visitEmbeddedExpression(@NotNull DrawNMapParser.EmbeddedExpressionContext ctx) {
                return reduceSource(ctx.expression(), idToCellMap);
            }
        });
    }
}

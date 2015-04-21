package astava.samples.drawnmap;

import astava.samples.drawnmap.lang.antlr4.DrawNMapBaseVisitor;
import astava.samples.drawnmap.lang.antlr4.DrawNMapLexer;
import astava.samples.drawnmap.lang.antlr4.DrawNMapParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
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
        private final ComponentListener componentListener;

        private Selection(JComponent componentOver, JComponent marking, String variableName, ComponentListener componentListener) {
            this.component = componentOver;
            this.marking = marking;
            this.variableName = variableName;
            this.componentListener = componentListener;
        }
    }

    private ArrayList<Selection> selections = new ArrayList<>();

    private JPanel selectionsOverlay;

    @Override
    public void beginSelect() {
        selectionsOverlay = new JPanel();
        selectionsOverlay.setSize(canvasView.getSize());
        selectionsOverlay.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (MouseListener l : canvasView.getMouseListeners())
                    l.mouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                for (MouseListener l : canvasView.getMouseListeners())
                    l.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for (MouseListener l : canvasView.getMouseListeners())
                    l.mouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                for (MouseListener l : canvasView.getMouseListeners())
                    l.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                for (MouseListener l : canvasView.getMouseListeners())
                    l.mouseExited(e);
            }
        });
        selectionsOverlay.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                for (MouseMotionListener l : canvasView.getMouseMotionListeners())
                    l.mouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                for (MouseMotionListener l : canvasView.getMouseMotionListeners())
                    l.mouseMoved(e);
            }
        });
        selectionsOverlay.setOpaque(false);

        canvasView.add(selectionsOverlay);
        canvasView.setLayer(selectionsOverlay, JLayeredPane.DRAG_LAYER + 1);

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
        canvasView.remove(selectionsOverlay);
        selectionsOverlay = null;
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
    public void select(String variableName, JComponent component) {
        JPanel marking = new JPanel(new BorderLayout());
        marking.setToolTipText(variableName);
        if(variableName == null)
            variableName = nextVariableName();
        TitledBorder border = new TitledBorder(variableName);
        border.setTitleColor(Color.DARK_GRAY);
        border.setTitleFont(new Font(Font.MONOSPACED, Font.BOLD | Font.ITALIC, 16));
        marking.setBorder(border);
        marking.setOpaque(false);

        int sizeExtra = 6;
        int topExtra = 10;

        canvasView.add(marking, JLayeredPane.DRAG_LAYER);
        canvasView.revalidate();
        canvasView.repaint();

        ComponentListener componentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                marking.setSize(sizeExtra + component.getWidth() + sizeExtra, topExtra + sizeExtra + component.getHeight() + sizeExtra);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                marking.setLocation(component.getX() - sizeExtra, component.getY() - (topExtra + sizeExtra));
            }
        };

        componentListener.componentResized(new ComponentEvent(component, -1));
        componentListener.componentMoved(new ComponentEvent(component, -1));

        component.addComponentListener(componentListener);

        selections.add(new Selection(component, marking, variableName, componentListener));

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
            s.component.removeComponentListener(s.componentListener);
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

    private Function<Object[], Object> resolve(String name, Object[] arguments) {
        java.util.List<Function<Object[], Object>> candidates = functions.entrySet().stream()
            .filter(x ->
                x.getKey().name.equals(name))
            .filter(x ->
                x.getKey().parameterTypes.length == arguments.length)
            .filter(x ->
                IntStream.range(0, arguments.length).allMatch(i ->
                    x.getKey().parameterTypes[i].isAssignableFrom(arguments[i].getClass())))
            .map(x -> x.getValue())
            .collect(Collectors.toList());

        if(candidates.size() > 0) {
            // TODO: Compare candidates; select "most specific".
            return candidates.get(0);
        }

        return null;
    }

    private Hashtable<Selector, Function<Object[], Object>> functions = new Hashtable<>();

    private void define(String name, Class<?>[] parameterTypes, Function<Object[], Object> function) {
        functions.put(new Selector(name, parameterTypes), function);
    }

    private void defineWrapper(String name, Class<?>[] parameterTypes, Function<Object[], Function<Function<Object, SlotValueComponent>, SlotValueComponent>> wrapper) {
        functions.put(new Selector(name, parameterTypes), args -> wrapper.apply(args));
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

        define("line", new Class<?>[]{BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class}, arguments ->
            new Line(((BigDecimal) arguments[0]).intValue(), ((BigDecimal) arguments[1]).intValue(), ((BigDecimal) arguments[2]).intValue(), ((BigDecimal) arguments[3]).intValue()));

        defineWrapper("bounds", new Class<?>[]{Object.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class}, args -> valueCreator -> {
            SlotValueComponent svc = valueCreator.apply(args[0]);
            svc.getComponent().setBounds(((BigDecimal) args[1]).intValue(), ((BigDecimal) args[2]).intValue(), ((BigDecimal) args[3]).intValue(), ((BigDecimal) args[4]).intValue());
            return svc;
        });
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

    private int nextOutX;
    private int nextOutY;

    private void updateOuts(int width, int height) {
        nextOutY += height + 30;
    }

    private SlotValueComponent createSlotNumber(Slot slot, BigDecimal value) {
        return new SlotValueComponent() {
            private JFormattedTextField component;

            {
                NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
                nf.setParseIntegerOnly(false);
                NumberFormatter formatter = new NumberFormatter(nf);
                formatter.setValueClass(BigDecimal.class);
                component = new JFormattedTextField(formatter);

                component.addPropertyChangeListener("value", evt -> {
                    BigDecimal currentValue = (BigDecimal) component.getValue();
                    if (currentValue != null)
                        slot.set(currentValue);
                });

                component.setValue(value);
                component.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public boolean accepts(Object value) {
                return value instanceof BigDecimal;
            }

            @Override
            public void setValue(Object value) {
                component.setValue(value);
            }
        };
    }

    private SlotValueComponent createSlotText(Slot slot, String value) {
        return new SlotValueComponent() {
            private JFormattedTextField component;

            {
                component = new JFormattedTextField(new DefaultFormatter());

                component.addPropertyChangeListener("value", evt -> {
                    String currentValue = (String) component.getValue();
                    if (currentValue != null)
                        slot.set(currentValue);
                });

                component.setValue(value);
                component.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public boolean accepts(Object value) {
                return value instanceof String;
            }

            @Override
            public void setValue(Object value) {
                component.setValue(value);
            }
        };
    }

    private SlotValueComponent createSlotDefault(Slot slot, Object value) {
        return new SlotValueComponent() {
            private JFormattedTextField component;

            {
                component = new JFormattedTextField(new DefaultFormatter());
                component.setEditable(false);

                component.addPropertyChangeListener("value", evt -> {
                    Object currentValue = component.getValue();
                    if (currentValue != null)
                        slot.set(currentValue);
                });

                component.setValue(value);
                component.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public boolean accepts(Object value) {
                return value instanceof Object[];
            }

            @Override
            public void setValue(Object value) {
                component.setValue(value);
            }
        };
    }

    private SlotValueComponent createSlotLine(Slot slot, Line value) {
        return new SlotValueComponent() {
            private LineTool.Line component;

            {
                component = new LineTool.Line(value.x1, value.y1, value.x2, value.y2);
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public boolean accepts(Object value) {
                return value instanceof Line;
            }

            @Override
            public void setValue(Object value) {
                component.setLine(((Line) value).x1, ((Line) value).y1, ((Line) value).x2, ((Line) value).y2);
            }
        };
    }

    private void evaluateProgram(DrawNMapParser.ProgramContext programCtx) {
        nextOutX = 30;
        nextOutY = 30;

        programCtx.accept(new DrawNMapBaseVisitor<Void>() {
            @Override
            public Void visitPropertyAssign(@NotNull DrawNMapParser.PropertyAssignContext ctx) {
                String variableName = ctx.target.ID().getText();
                String propertyName = ctx.name.ID().getText();
                SlotComponent currentTarget = (SlotComponent) environment.get(variableName);

                Map<String, Cell> idToCellMap = new Hashtable<>();
                Cell<Object> source = (Cell<Object>) reduceSource(ctx.expression(), idToCellMap, new ArrayList<>());
                currentTarget.propertyAssign(propertyName, source);

                return super.visitPropertyAssign(ctx);
            }

            @Override
            public Void visitAssign(@NotNull DrawNMapParser.AssignContext ctx) {
                Map<String, Cell> idToCellMap = new Hashtable<>();

                String variableName = ctx.ID().getText();
                CellConsumer<Object> currentTarget = (CellConsumer<Object>) environment.get(variableName);

                Cell<Object> source = (Cell<Object>) reduceSource(ctx.expression(), idToCellMap, new ArrayList<>());

                String srcCode = ctx.getText();

                if(currentTarget == null) {
                    SlotComponent newElement = new SlotComponent(new SlotValueComponentFactory() {
                        boolean atFirst = true;

                        @Override
                        public SlotValueComponent createSlotComponentValue(JPanel wrapper, Slot slot, Object value) {
                            if (value instanceof BigDecimal) {
                                SlotValueComponent svc = createSlotNumber(slot, (BigDecimal) value);
                                if(atFirst) {
                                    svc.getComponent().setSize(60, 20);
                                    svc.getComponent().setLocation(nextOutX, nextOutY);

                                    updateOuts(svc.getComponent().getWidth(), svc.getComponent().getHeight());
                                }
                                atFirst = false;
                                return svc;
                            } else if (value instanceof String) {
                                SlotValueComponent svc = createSlotText(slot, (String) value);
                                if(atFirst) {
                                    svc.getComponent().setSize(60, 20);
                                    svc.getComponent().setLocation(nextOutX, nextOutY);

                                    updateOuts(svc.getComponent().getWidth(), svc.getComponent().getHeight());
                                }
                                atFirst = false;
                                return svc;
                            } else if (value instanceof Line) {
                                atFirst = false;
                                return createSlotLine(slot, (Line) value);
                            } else if(value instanceof Function) {
                                return ((Function<Function<Object, SlotValueComponent>, SlotValueComponent>)value).apply(v ->
                                    createSlotComponentValue(wrapper, slot, v));
                            } else {
                                SlotValueComponent svc = createSlotDefault(slot, value);
                                if(atFirst) {
                                    svc.getComponent().setSize(60, 20);
                                    svc.getComponent().setLocation(nextOutX, nextOutY);

                                    updateOuts(svc.getComponent().getWidth(), svc.getComponent().getHeight());
                                }
                                atFirst = false;
                                return svc;
                            }
                        }
                    });

                    idToCellMap.put(variableName, newElement);

                    currentTarget = newElement;
                    canvasView.add(newElement);

                    select(variableName, newElement);
                } else {
                    idToCellMap.put(variableName, (Cell) currentTarget);
                }

                Binding binding = source.consume(currentTarget);
                currentTarget.setBinding(binding);
                currentTarget.setDescription(new Description(idToCellMap, srcCode));

                return null;
            }

            @Override
            public Void visitFunction(@NotNull DrawNMapParser.FunctionContext ctx) {
                String functionName = ctx.ID().getText();

                ArrayList<ParameterInfo> parameterTypes = new ArrayList<>();
                if(ctx.parameters() != null)
                    parameterTypes.addAll(ctx.parameters().ID().stream().map(x -> new ParameterInfo(Object.class, x.getText())).collect(Collectors.toList()));

                ParserRuleContext bodyTree = ctx.expression();

                /*bodyTree.accept(new DrawNMapBaseVisitor<Void>() {
                    @Override
                    public Void visitParameterAndUsage(@NotNull DrawNMapParser.ParameterAndUsageContext ctx) {
                        String parameterName = ctx.ID().getText();
                        if (parameterTypes.stream().noneMatch(x -> x.name.equals(parameterName)))
                            parameterTypes.add(new ParameterInfo(Object.class, parameterName));

                        return super.visitParameterAndUsage(ctx);
                    }
                });*/

                //Selector selector = new Selector(functionName, parameterTypes.stream().map(x -> x.type).toArray(s -> new Class<?>[s]));

                Cell<?> cellBody = reduceSource(bodyTree, new Hashtable<>(), parameterTypes);
                Function<Object[], Object> body = args -> cellBody.value(args);
                define(functionName, parameterTypes.stream().map(x -> x.type).toArray(s -> new Class<?>[s]), body);

                return null;
            }
        });
    }

    private static class ParameterInfo {
        public final Class<?> type;
        public final String name;

        private ParameterInfo(Class<?> type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    private Cell<Object> createFunctionCall(String name, java.util.List<Cell<Object>> argumentCells) {
        return new Cell<Object>() {
            @Override
            public Binding consume(CellConsumer<Object> consumer) {
                return new Binding() {
                    private java.util.List<Object> arguments = new ArrayList<>();
                    private java.util.List<Binding> bindings;

                    {
                        IntStream.range(0, argumentCells.size()).forEach(i -> arguments.add(null));
                        bindings = IntStream.range(0, argumentCells.size()).mapToObj(i -> argumentCells.get(i).consume(next -> {
                            arguments.set(i, next);
                            update();
                        })).collect(Collectors.toList());
                    }

                    private void update() {
                        if(arguments.stream().filter(x -> x == null).count() == 0) {
                            Function<Object[], Object> function = resolve(name, arguments.toArray());

                            Object next;

                            if(function != null)
                                next = function.apply(arguments.toArray(new Object[arguments.size()]));
                            else
                                next = null;

                            consumer.next(next);
                        }
                    }

                    @Override
                    public void remove() {
                        bindings.forEach(x -> x.remove());
                        arguments = null;
                        bindings = null;
                    }
                };
            }

            @Override
            public Object value(Object[] args) {
                java.util.List<Object> arguments = argumentCells.stream().map(x -> x.value(args)).collect(Collectors.toList());
                Class<?>[] parameterTypes = arguments.stream().map(x -> x.getClass()).toArray(s -> new Class<?>[s]);
                Function<Object[], Object> function = functions.get(new Selector(name, parameterTypes));

                Object next;

                if(function != null)
                    next = function.apply(arguments.toArray(new Object[arguments.size()]));
                else
                    next = null;

                return next;
            }
        };
    }

    private Cell<Object> createBinaryOperation(String operator, Cell<Object> lhsCell, Cell<Object> rhsCell) {
        return createFunctionCall(operator, Arrays.asList(lhsCell, rhsCell));
    }

    private Object reduce(String operator, Object lhs, Object rhs) {
        Function<Object[], Object> function = functions.get(new Selector(operator, new Class<?>[]{lhs.getClass(), rhs.getClass()}));

        if(function != null)
            return function.apply(new Object[]{lhs, rhs});

        return null;
    }

    private Cell<?> reduceSource(ParserRuleContext ctx, Map<String, Cell> idToCellMap, ArrayList<ParameterInfo> parameters) {
        return ctx.accept(new DrawNMapBaseVisitor<Cell>() {
            @Override
            public Cell visitAddExpression(@NotNull DrawNMapParser.AddExpressionContext ctx) {
                Cell lhs = reduceSource(ctx.mulExpression(0), idToCellMap, parameters);

                if(ctx.mulExpression().size() > 1) {
                    for(int i = 1; i < ctx.mulExpression().size(); i++) {
                        Cell<Object> rhsCell = (Cell<Object>)reduceSource(ctx.mulExpression(i), idToCellMap, parameters);

                        Cell<Object> lhsCell = (Cell<Object>)lhs;

                        String operator = ctx.ADD_OP(i - 1).getText();

                        lhs = createBinaryOperation(operator, lhsCell, rhsCell);
                    }
                }

                return lhs;
            }

            @Override
            public Cell visitMulExpression(@NotNull DrawNMapParser.MulExpressionContext ctx) {
                Cell lhs = reduceSource(ctx.leafExpression(0), idToCellMap, parameters);

                if(ctx.leafExpression().size() > 1) {
                    for(int i = 1; i < ctx.leafExpression().size(); i++) {
                        Cell<Object> rhsCell = (Cell<Object>)reduceSource(ctx.leafExpression(i), idToCellMap, parameters);

                        Cell<Object> lhsCell = (Cell<Object>)lhs;

                        String operator = ctx.MUL_OP(i - 1).getText();

                        lhs = createBinaryOperation(operator, lhsCell, rhsCell);
                    }
                }

                return lhs;
            }

            @Override
            public Cell visitFunctionCall(@NotNull DrawNMapParser.FunctionCallContext ctx) {
                String name = ctx.id().ID().getText();

                java.util.List<Cell<Object>> argumentCells = ctx.expression().stream().map(x -> (Cell<Object>) reduceSource(x, idToCellMap, parameters)).collect(Collectors.toList());

                return createFunctionCall(name, argumentCells);
            }

            @Override
            public Cell visitProperty(@NotNull DrawNMapParser.PropertyContext ctx) {
                String name = ctx.name.ID().getText();
                String id = ctx.target.ID().getText();
                Cell cell = environment.get(id);

                idToCellMap.put(id, cell);

                return ((SlotComponent)cell).property(name);
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
            public Cell visitArray(@NotNull DrawNMapParser.ArrayContext ctx) {
                java.util.List<Cell> valueCells = ctx.expression().stream().map(x -> reduceSource(x, idToCellMap, parameters)).collect(Collectors.toList());

                // Could be a usage of a/the list function?

                return new Cell<Object>() {
                    @Override
                    public Binding consume(CellConsumer<Object> consumer) {
                        return new Binding() {
                            private java.util.List<Object> arguments = new ArrayList<>();
                            private java.util.List<Binding> bindings;

                            {
                                IntStream.range(0, valueCells.size()).forEach(i -> arguments.add(null));
                                bindings = IntStream.range(0, valueCells.size()).mapToObj(i -> valueCells.get(i).consume(next -> {
                                    arguments.set(i, next);
                                    update();
                                })).collect(Collectors.toList());
                            }

                            private void update() {
                                if(arguments.stream().filter(x -> x == null).count() == 0) {
                                    Object next = value(null);//valueCells.stream().map(x -> x.value(null)).toArray(s -> new Object[s]);

                                    consumer.next(next);
                                }
                            }

                            @Override
                            public void remove() {
                                bindings.forEach(x -> x.remove());
                                arguments = null;
                                bindings = null;
                            }
                        };
                    }

                    @Override
                    public Object value(Object[] args) {
                        return new Tuple(valueCells.stream().map(x -> x.value(args)).toArray(s -> new Object[s]));
                    }
                };
            }

            @Override
            public Cell visitBlock(@NotNull DrawNMapParser.BlockContext ctx) {
                ArrayList<ParameterInfo> parameters = new ArrayList<>();
                Cell bodyCell = reduceSource(ctx.expression(), new Hashtable<>(), parameters);

                return new Singleton(new Block(bodyCell));
            }

            @Override
            public Cell visitParameterAndUsage(@NotNull DrawNMapParser.ParameterAndUsageContext ctx) {
                String parameterName = ctx.ID().getText();
                int ordinal =
                    IntStream.range(0, parameters.size())
                        .filter(i -> parameters.get(i).name.equals(parameterName))
                        .findFirst()
                        .orElseGet(() -> {
                            parameters.add(new ParameterInfo(Object.class, parameterName));
                            return parameters.size() - 1;
                        });

                return new Cell() {
                    @Override
                    public Binding consume(CellConsumer consumer) {
                        return null;
                    }

                    @Override
                    public Object value(Object[] args) {
                        return args[ordinal];
                    }
                };
            }

            @Override
            public Cell visitEmbeddedExpression(@NotNull DrawNMapParser.EmbeddedExpressionContext ctx) {
                return reduceSource(ctx.expression(), idToCellMap, parameters);
            }
        });
    }
}

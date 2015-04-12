package astava.samples.drawnmap;

import astava.parse.CommonMatcher;
import astava.parse.Matcher;
import astava.parse.charsequence.CharSequenceCursor;
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
import java.util.Hashtable;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class MainView extends JFrame {
    private java.util.List<Tool> tools;
    private JComponent toolBoxView;
    private JLayeredPane canvasView;
    private JComponent scriptView;
    private Hashtable<String, Cell> environment = new Hashtable<>();

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

        return view;
    }

    private void evaluateProgram(DrawNMapParser.ProgramContext programCtx) {
        programCtx.accept(new DrawNMapBaseVisitor<Void>() {
            @Override
            public Void visitAssign(@NotNull DrawNMapParser.AssignContext ctx) {
                CellConsumer<Object> target = (CellConsumer<Object>) environment.get(ctx.ID().getText());
                Cell<Object> source = (Cell<Object>) reduceSource(ctx.expression());

                source.consume(target);

                return null;
            }
        });
    }

    private Cell<BigDecimal> createBinaryOperation(String operator, Cell<BigDecimal> lhsCell, Cell<BigDecimal> rhsCell) {
        return new Cell<BigDecimal>() {
            @Override
            public Binding consume(CellConsumer<BigDecimal> consumer) {
                return new Binding() {
                    private BigDecimal lhsValue;
                    private BigDecimal rhsValue;

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
                            BigDecimal next = reduce(operator, lhsValue, rhsValue);
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
        };
    }

    private BigDecimal reduce(String operator, BigDecimal lhs, BigDecimal rhs) {
        switch(operator) {
            case "+":
                return lhs.add(rhs);
            case "-":
                return lhs.subtract(rhs);
            case "*":
                return lhs.multiply(rhs);
            case "/":
                return lhs.divide(rhs);
        }

        return null;
    }

    private Cell<?> reduceSource(ParserRuleContext ctx) {
        return ctx.accept(new DrawNMapBaseVisitor<Cell>() {
            @Override
            public Cell visitAddExpression(@NotNull DrawNMapParser.AddExpressionContext ctx) {
                Cell lhs = reduceSource(ctx.mulExpression(0));

                if(ctx.mulExpression().size() > 1) {
                    for(int i = 1; i < ctx.mulExpression().size(); i++) {
                        Cell<BigDecimal> rhsCell = (Cell<BigDecimal>)reduceSource(ctx.mulExpression(i));

                        Cell<BigDecimal> lhsCell = (Cell<BigDecimal>)lhs;

                        String operator = ctx.ADD_OP(i - 1).getText();

                        lhs = createBinaryOperation(operator, lhsCell, rhsCell);
                    }
                }

                return lhs;
            }

            @Override
            public Cell visitMulExpression(@NotNull DrawNMapParser.MulExpressionContext ctx) {
                Cell lhs = reduceSource(ctx.leafExpression(0));

                if(ctx.leafExpression().size() > 1) {
                    for(int i = 1; i < ctx.leafExpression().size(); i++) {
                        Cell<BigDecimal> rhsCell = (Cell<BigDecimal>)reduceSource(ctx.leafExpression(i));

                        Cell<BigDecimal> lhsCell = (Cell<BigDecimal>)lhs;

                        String operator = ctx.MUL_OP(i - 1).getText();

                        lhs = createBinaryOperation(operator, lhsCell, rhsCell);
                    }
                }

                return lhs;
            }

            @Override
            public Cell visitId(@NotNull DrawNMapParser.IdContext ctx) {
                return environment.get(ctx.ID().getText());
            }

            @Override
            public Cell visitNumber(@NotNull DrawNMapParser.NumberContext ctx) {
                return new Singleton<>(new BigDecimal(ctx.NUMBER().getText()));
            }

            @Override
            public Cell visitEmbeddedExpression(@NotNull DrawNMapParser.EmbeddedExpressionContext ctx) {
                return reduceSource(ctx.expression());
            }
        });
    }
}

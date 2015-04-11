package astava.samples.virela.view;

import astava.parse.CommonMatcher;
import astava.parse.Matcher;
import astava.parse.charsequence.CharSequenceCursor;
import astava.samples.virela.parser.*;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RelationSetView extends JPanel {
    //private Map<String, Slot<?>> relationSet = new Hashtable<>();
    private Dict root;
    private JPanel contentView;
    private JTextPane scriptView;

    private ScheduledExecutorService parseExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> parseFuture;
    private Map<String, Object> relationSetStates = new Hashtable<>();

    private ExecutorService propogator = Executors.newSingleThreadExecutor();

    public RelationSetView() {
        setLayout(new BorderLayout());

        contentView = new JPanel();
        contentView.setLayout(new BoxLayout(contentView, BoxLayout.Y_AXIS));

        add(contentView, BorderLayout.CENTER);

        scriptView = new JTextPane();
        scriptView.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    runScript();
                }
            }
        });
        scriptView.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));



        scriptView.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        add(scriptView, BorderLayout.SOUTH);
    }

    private void runScript() {
        String source = scriptView.getText();
        System.out.println("Source:\n" + source);

        ScriptParser parser = new ScriptParser();
        Matcher<Character, Statement> matcher = parser.parseInit(new CharSequenceCursor(source), (p, c) -> new CommonMatcher<>());

        if (matcher.isMatch()) {
            matcher.production().stream().forEach(s -> runStatement(s));

            contentView.revalidate();
            contentView.repaint();

            //System.out.println("Success, relation set size = " + relationSet.size());
        } else {
            System.out.println("Failure");
        }
    }

    private void runStatement(Statement statement) {
        statement.accept(new StatementVisitor() {
            @Override
            public void visitAssignLazy(String id, Expression value) {
                Cell<?> valueCell = expressionToView(value);

                root.put(id, valueCell);
                //Slot slot = relationSet.computeIfAbsent(id, i -> new Slot<>());

                //slot.set(valueCell);
            }

            @Override
            public void visitAssignEager(String id, Expression value) {

            }
        });
    }

    private Cell<?> expressionToView(Expression expression) {
        return expression.reduce(new ExpressionReducer<Cell<?>>() {
            /*@Override
            public void visitNumberStream() {


                NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
                nf.setParseIntegerOnly(false);
                NumberFormatter formatter = new NumberFormatter(nf);
                formatter.setValueClass(BigDecimal.class);
                JFormattedTextField view = new JFormattedTextField(formatter);
                view.setValue(new BigDecimal(0));
                view.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

                reduceTo(new Cell<BigDecimal>(view) {
                    private ArrayList<BigDecimal> buffer = new ArrayList<>();
                    private ArrayList<CellConsumer<BigDecimal>> consumers = new ArrayList<>();

                    {
                        view.addPropertyChangeListener("value", evt -> {
                            BigDecimal currentValue = (BigDecimal) view.getValue();
                            if (currentValue != null)
                                propogateNext(currentValue);
                        });

                        BigDecimal currentValue = (BigDecimal) view.getValue();
                        propogateNext(currentValue);
                    }

                    @Override
                    public void consume(CellConsumer<BigDecimal> consumer) {
                        System.out.println("IntStream.consume: " + this);
                        System.out.println("buffer: " + buffer);
                        propogateBuffer(0, consumer);
                    }

                    @Override
                    public String toString() {
                        return "int: " + view.getValue();
                    }

                    @Override
                    public Object getState() {
                        return view.getValue();
                    }

                    @Override
                    public void loadState(Object state) {
                        if (state != null && state instanceof BigDecimal) {
                            view.setValue(state);
                        }
                    }

                    private void propogateNext(BigDecimal next) {
                        System.out.println("propogateNext: " + next);
                        System.out.println("buffer: " + buffer);
                        buffer.add(next);
                        ArrayList<CellConsumer<BigDecimal>> currentConsumers = new ArrayList<>(consumers);
                        ArrayList<CellConsumer<BigDecimal>> nextConsumers = new ArrayList<>();

                        for (CellConsumer<BigDecimal> c : currentConsumers)
                            c.next(next, nc -> nextConsumers.add(nc));

                        consumers = nextConsumers;
                        System.out.println("nextConsumers=" + nextConsumers);
                    }

                    private void propogateBuffer(int index, CellConsumer<BigDecimal> consumer) {
                        if (index < buffer.size())
                            consumer.next(buffer.get(index), nc -> propogateBuffer(index + 1, nc));
                        else
                            consumers.add(consumer);
                    }
                });
            }*/

            @Override
            public void visitDict(List<Map.Entry<String, Expression>> entries) {
                Dict d = new Dict();

                entries.forEach(e -> d.put(e.getKey(), expressionToView(e.getValue())));

                reduceTo(d);
            }

            @Override
            public void visitLookup(Expression target, String id) {

            }

            @Override
            public void visitId(String id) {
                //Slot slot = relationSet.computeIfAbsent(id, i -> new Slot());
                Cell<?> slot = root.get(id);

                reduceTo(slot);

                /*if(relationSet.containsKey(id)) {
                    JLabel label = new JLabel(id);

                    if(id.equals("x")) {
                        System.out.println(relationSet);
                        new String();
                    }

                    ((Cell<Object>)relationSet.get(id)).consume(new CellConsumer.Infinite<Object>() {
                        @Override
                        public void next(Object value) {
                            label.setText(value.toString());
                            label.revalidate();
                            label.repaint();
                            System.out.println("text=" + label.getText());
                        }
                    });

                    reduceTo(new Cell<Object>(label) {
                        @Override
                        public void consume(CellConsumer<Object> consumer) {
                            if (relationSet.containsKey(id)) {
                                ((Cell<Object>) relationSet.get(id)).consume(consumer);
                            }
                        }
                    });
                } else {
                    JLabel label = new JLabel("Undefined");

                    reduceTo(new Cell<Object>(label) {
                        @Override
                        public void consume(CellConsumer<Object> consumer) {

                        }
                    });
                }*/
            }

            @Override
            public void visitNumberLiteral(BigDecimal value) {
                //JLabel view = new JLabel("" + value);

                reduceTo(new Singleton<>(value));

                /*reduceTo(new Cell<BigDecimal>() {
                    BigDecimal theValue = value;

                    @Override
                    public Binding consume(CellConsumer<BigDecimal> consumer) {
                        consumer.next(theValue);

                        return () -> { };
                    }
                });*/
            }

            @Override
            public void visitConstruction(String id, List<Map.Entry<String, Expression>> arguments) {

            }

            @Override
            public void visitBinary(int operator, Expression lhs, Expression rhs) {
                Cell<BigDecimal> lhsCell = (Cell<BigDecimal>)expressionToView(lhs);
                Cell<BigDecimal> rhsCell = (Cell<BigDecimal>)expressionToView(rhs);

                //JLabel label = new JLabel();

                reduceTo(new Cell<BigDecimal>() {
                    /*private BigDecimal lhsValue;
                    private BigDecimal rhsValue;

                    private ArrayList<BigDecimal> buffer = new ArrayList<>();
                    private ArrayList<CellConsumer<BigDecimal>> consumers = new ArrayList<>();

                    {
                        lhsCell.consume(new CellConsumer<BigDecimal>() {
                            @Override
                            public void next(BigDecimal value) {
                                lhsValue = value;
                                update();
                            }
                        });
                        rhsCell.consume(new CellConsumer<BigDecimal>() {
                            @Override
                            public void next(BigDecimal value) {
                                rhsValue = value;
                                update();
                            }
                        });
                    }

                    private void update() {
                        if(lhsValue != null && rhsValue != null) {
                            System.out.println("update");

                            BigDecimal next = null;

                            try {
                                switch (operator) {
                                    case ExpressionVisitor.BINARY_OPERATOR_ADD:
                                        //next = lhsValue.intValue() + rhsValue.intValue();
                                        next = lhsValue.add(rhsValue);
                                        break;
                                    case ExpressionVisitor.BINARY_OPERATOR_SUB:
                                        //next = lhsValue.intValue() - rhsValue.intValue();
                                        next = lhsValue.subtract(rhsValue);
                                        break;
                                    case ExpressionVisitor.BINARY_OPERATOR_MUL:
                                        //next = lhsValue.intValue() * rhsValue.intValue();
                                        next = lhsValue.multiply(rhsValue);
                                        break;
                                    case ExpressionVisitor.BINARY_OPERATOR_DIV:
                                        //next = lhsValue.intValue() / rhsValue.intValue();
                                        next = lhsValue.divide(rhsValue);
                                        break;
                                }

                                //label.setText(next.toString());

                                propogateNext(next);
                            } catch(ArithmeticException e) {
                                //label.setText("NAN");
                            }
                        }
                    }*/

                    @Override
                    public Binding consume(CellConsumer<BigDecimal> consumer) {
                        //propogateBuffer(0, consumer);

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
                                    System.out.println("update");

                                    BigDecimal next = null;

                                    try {
                                        switch (operator) {
                                            case ExpressionVisitor.BINARY_OPERATOR_ADD:
                                                //next = lhsValue.intValue() + rhsValue.intValue();
                                                next = lhsValue.add(rhsValue);
                                                break;
                                            case ExpressionVisitor.BINARY_OPERATOR_SUB:
                                                //next = lhsValue.intValue() - rhsValue.intValue();
                                                next = lhsValue.subtract(rhsValue);
                                                break;
                                            case ExpressionVisitor.BINARY_OPERATOR_MUL:
                                                //next = lhsValue.intValue() * rhsValue.intValue();
                                                next = lhsValue.multiply(rhsValue);
                                                break;
                                            case ExpressionVisitor.BINARY_OPERATOR_DIV:
                                                //next = lhsValue.intValue() / rhsValue.intValue();
                                                next = lhsValue.divide(rhsValue);
                                                break;
                                        }

                                        consumer.next(next);
                                    } catch(ArithmeticException e) {
                                        //label.setText("NAN");
                                    }
                                }
                            }

                            @Override
                            public void remove() {
                                lhsBinding.remove();
                                rhsBinding.remove();
                            }
                        };
                    }

                    /*private void propogateNext(BigDecimal next) {
                        System.out.println("propogateNext");
                        buffer.add(next);
                        ArrayList<CellConsumer<BigDecimal>> currentConsumers = new ArrayList<>(consumers);
                        ArrayList<CellConsumer<BigDecimal>> nextConsumers = new ArrayList<>();

                        for (CellConsumer<BigDecimal> c : currentConsumers)
                            c.next(next, nc -> nextConsumers.add(nc));

                        consumers = nextConsumers;
                    }

                    private void propogateBuffer(int index, CellConsumer<BigDecimal> consumer) {
                        if(index < buffer.size())
                            consumer.next(buffer.get(index), nc -> propogateBuffer(index + 1, nc));
                        else
                            consumers.add(consumer);
                    }*/
                });
            }
        });
    }
}

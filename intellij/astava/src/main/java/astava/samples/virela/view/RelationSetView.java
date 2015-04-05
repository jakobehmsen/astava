package astava.samples.virela.view;

import astava.parse.CommonMatcher;
import astava.parse.Matcher;
import astava.parse.charsequence.CharSequenceCursor;
import astava.samples.virela.parser.*;
import javafx.scene.text.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RelationSetView extends JPanel {
    private Map<String, Cell<?>> relationSet;
    private JPanel contentView;

    private ScheduledExecutorService parseExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> parseFuture;
    private Map<String, Object> relationSetStates = new Hashtable<>();

    private ExecutorService propogator = Executors.newSingleThreadExecutor();

    public RelationSetView() {
        setLayout(new BorderLayout());

        contentView = new JPanel();
        contentView.setLayout(new BoxLayout(contentView, BoxLayout.Y_AXIS));

        add(contentView, BorderLayout.CENTER);

        JTextPane scriptView = new JTextPane();
        scriptView.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (parseFuture != null) {
                    parseFuture.cancel(false);
                }

                parseFuture = parseExecutor.schedule(() -> {
                    try {
                        String source = scriptView.getText();
                        System.out.println("Source:\n" + source);

                        if (source.contains("(") && source.contains(")")) {
                            new String();
                        }

                    /*

                    x = int
                    y = x + int // Makes sense?
                    // Should int be root level only expressions? Not subexpression?

                    */

                        RelationParser parser = new RelationParser();
                        Matcher<Character, Relation> matcher = parser.parseInit(new CharSequenceCursor(source), (p, c) -> new CommonMatcher<>());

                        if (matcher.isMatch()) {
                            contentView.removeAll();
                            //relationSet.values().forEach(x -> contentView.remove(x));

                            if (relationSet != null) {
                                /*if(relationSet.entrySet().stream().filter(x -> x.getKey() == null || x.getValue() == null).count() > 0) {
                                    new String();
                                }*/

                                relationSetStates.putAll(
                                    relationSet.entrySet().stream().filter(x -> x.getValue().getState() != null).collect(Collectors.toMap(x ->
                                        x.getKey(), x ->
                                        x.getValue().getState()))
                                );
                            }

                            java.util.List<Relation> newRelations = matcher.production().stream().collect(Collectors.toList());

                            relationSet = new Hashtable<>();


                            //relationSet = matcher.production().stream().collect(
                            //    Collectors.toMap(r -> r.getId(), r -> expressionToView(r.getValue())));

                            System.out.println("Ids: " + newRelations.stream().map(r -> r.getId()).collect(Collectors.toSet()));
                            System.out.println("Components count: " + contentView.getComponentCount());

                            //java.util.List<Cell<?>> newCells = newRelations.stream().map(r -> expressionToView(r.getValue())).collect(Collectors.toList());

                            newRelations.forEach(r -> {
                                // Insertion/update
                                // View should be dependent on expression:
                                // E.g. int stream should be numeric up down

                                JLabel relationIdView = new JLabel(r.getId());
                                relationIdView.setFont(new Font(Font.MONOSPACED, Font.ITALIC | Font.BOLD, 14));
                                relationIdView.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
                                JPanel topView = new JPanel(new BorderLayout());
                                topView.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
                                topView.add(relationIdView, BorderLayout.NORTH);
                                //relationIdView.setVerticalAlignment(JLabel.TOP);
                                //relationIdView.setVerticalTextPosition(JLabel.TOP);
                                Cell<?> cell = expressionToView(r.getValue());
                                //Cell<?> cell = relationSet.get(r.getId());
                                JComponent relationValueView = cell.getView();
                                relationValueView.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                                JPanel relationView = new JPanel(new BorderLayout());
                                relationView.setBackground(Color.WHITE);
                                relationView.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                                relationView.add(topView, BorderLayout.WEST);
                                relationView.add(relationValueView);

                                relationView.setMaximumSize(new Dimension(relationView.getMaximumSize().width, 30));
                                contentView.add(relationView);
                                relationSet.put(r.getId(), cell);

                                if (relationSetStates.containsKey(r.getId())) {
                                    Object cellState = relationSetStates.get(r.getId());

                                    cell.loadState(cellState);
                                }

                            /*if (!relationSet.containsKey(r.getId())) {
                                // Insertion
                                // View should be dependent on expression:
                                // E.g. int stream should be numeric up down

                                JLabel relationIdView = new JLabel(r.getId());
                                JComponent relationValueView = expressionToView(r.getValue());

                                JPanel relationView = new JPanel();
                                relationView.add(relationIdView);

                                relationSet.put(r.getId(), relationView);
                                contentView.add(relationView);
                            } else {
                                // Update
                                JComponent relationView = relationSet.get(r.getId());
                                contentView.add(relationView);
                            }*/
                            });

                            //// Deletions
                            //relationSet.keySet().retainAll(newRelations.stream().map(r -> r.getId()).collect(Collectors.toSet()));

                            contentView.revalidate();
                            contentView.repaint();

                            System.out.println("Success, relation set size = " + relationSet.size());
                        } else {
                            System.out.println("Failure");
                        }
                    } catch (Exception ex) {
                        System.out.println("Unexpected error occured during parse: ");
                        ex.printStackTrace();
                    }
                }, 150, TimeUnit.MILLISECONDS);
            }
        });
        scriptView.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));



        scriptView.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        add(scriptView, BorderLayout.SOUTH);
    }

    private Cell<?> expressionToView(Expression expression) {
        return expression.reduce(new ExpressionReducer<Cell<?>>() {
            @Override
            public void visitNumberStream() {
                /*NumberFormat format = NumberFormat.getInstance();
                NumberFormatter formatter = new NumberFormatter(format);
                formatter.setValueClass(BigDecimal.class);*/


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

                        /*view.getDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                BigDecimal currentValue = (BigDecimal) view.getValue();
                                if (currentValue != null)
                                    propogateNext(currentValue);
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                BigDecimal currentValue = (BigDecimal) view.getValue();
                                if (currentValue != null)
                                    propogateNext(currentValue);
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                BigDecimal currentValue = (BigDecimal) view.getValue();
                                if (currentValue != null)
                                    propogateNext(currentValue);
                            }
                        });*/

                        /*view.addActionListener(e -> {
                            BigDecimal currentValue = (BigDecimal) view.getValue();
                            propogateNext(currentValue);
                        });*/

                        /*((JSpinner) getView()).addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                BigDecimal currentValue = (BigDecimal) ((JSpinner) getView()).getValue();
                                propogateNext(currentValue);
                                System.out.println("stateChanged: " + currentValue.intValue());
                            }
                        });*/

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
            }

            @Override
            public void visitId(String id) {
                if(relationSet.containsKey(id)) {
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
                }
            }

            @Override
            public void visitNumberLiteral(BigDecimal value) {
                JLabel view = new JLabel("" + value);

                reduceTo(new Cell<BigDecimal>(view) {
                    BigDecimal theValue = value;

                    @Override
                    public void consume(CellConsumer<BigDecimal> consumer) {
                        consumer.next(theValue, nc -> nc.atEnd());
                    }

                    @Override
                    public Object getState() {
                        return theValue;
                    }

                    @Override
                    public void loadState(Object state) {
                        /*if (state != null && state instanceof BigDecimal) {
                            theValue = (BigDecimal)state;
                            view.setText("" + theValue);
                        }*/
                    }
                });
            }

            @Override
            public void visitBinary(int operator, Expression lhs, Expression rhs) {
                Cell<BigDecimal> lhsCell = (Cell<BigDecimal>)expressionToView(lhs);
                Cell<BigDecimal> rhsCell = (Cell<BigDecimal>)expressionToView(rhs);

                JLabel label = new JLabel();

                reduceTo(new Cell<BigDecimal>(label) {
                    private BigDecimal lhsValue;
                    private BigDecimal rhsValue;

                    private ArrayList<BigDecimal> buffer = new ArrayList<>();
                    private ArrayList<CellConsumer<BigDecimal>> consumers = new ArrayList<>();

                    {
                        lhsCell.consume(new CellConsumer.Infinite<BigDecimal>() {
                            @Override
                            public void next(BigDecimal value) {
                                lhsValue = value;
                                update();
                            }
                        });
                        rhsCell.consume(new CellConsumer.Infinite<BigDecimal>() {
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

                                label.setText(next.toString());

                                propogateNext(next);
                            } catch(ArithmeticException e) {
                                label.setText("NAN");
                            }
                        }
                    }

                    @Override
                    public void consume(CellConsumer<BigDecimal> consumer) {
                        propogateBuffer(0, consumer);
                    }

                    private void propogateNext(BigDecimal next) {
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
                    }
                });
            }
        });
    }
}

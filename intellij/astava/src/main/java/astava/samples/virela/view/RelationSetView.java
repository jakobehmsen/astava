package astava.samples.virela.view;

import astava.parse.CommonMatcher;
import astava.parse.Matcher;
import astava.parse.charsequence.CharSequenceCursor;
import astava.samples.virela.parser.*;
import javafx.scene.text.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RelationSetView extends JPanel {
    private Map<String, Cell<?>> relationSet;
    private JPanel contentView = new JPanel();

    private ScheduledExecutorService parseExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> parseFuture;
    private Map<String, Object> relationSetStates = new Hashtable<>();

    private ExecutorService propogator = Executors.newSingleThreadExecutor();

    public RelationSetView() {
        setLayout(new BorderLayout());

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

                            if(relationSet != null) {
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
                                relationIdView.setFont(new Font(Font.MONOSPACED, Font.ITALIC | Font.BOLD, 12));
                                relationIdView.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
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
                    } catch(Exception ex) {
                        System.out.println("Unexpected error occured during parse: ");
                        ex.printStackTrace();
                    }
                }, 150, TimeUnit.MILLISECONDS);
            }
        });
        scriptView.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        scriptView.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        add(scriptView, BorderLayout.SOUTH);
    }

    private Cell<?> expressionToView(Expression expression) {
        return expression.reduce(new ExpressionReducer<Cell<?>>() {
            @Override
            public void visitIntStream() {
                reduceTo(new Cell<Integer>(new JSpinner()) {
                    private ArrayList<Integer> buffer = new ArrayList<Integer>();
                    private ArrayList<CellConsumer<Integer>> consumers = new ArrayList<CellConsumer<Integer>>();

                    {
                        ((JSpinner)getView()).addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                Number currentValue = (Number)((JSpinner)getView()).getValue();
                                propogateNext(currentValue.intValue());
                                System.out.println("stateChanged: " + currentValue.intValue());
                            }
                        });

                        Number currentValue = (Number)((JSpinner)getView()).getValue();
                        propogateNext(currentValue.intValue());
                    }

                    @Override
                    public void consume(CellConsumer<Integer> consumer) {
                        System.out.println("IntStream.consume: " + this);
                        System.out.println("buffer: " + buffer);
                        propogateBuffer(0, consumer);
                    }

                    @Override
                    public String toString() {
                        return "int: " + ((JSpinner)getView()).getValue();
                    }

                    @Override
                    public Object getState() {
                        return (Number)((JSpinner)getView()).getValue();
                    }

                    @Override
                    public void loadState(Object state) {
                        if(state != null && state instanceof Number) {
                            ((JSpinner)getView()).setValue(state);
                        }
                    }

                    private void propogateNext(Integer next) {
                        System.out.println("propogateNext: " + next);
                        System.out.println("buffer: " + buffer);
                        buffer.add(next);
                        ArrayList<CellConsumer<Integer>> currentConsumers = new ArrayList<CellConsumer<Integer>>(consumers);
                        ArrayList<CellConsumer<Integer>> nextConsumers = new ArrayList<CellConsumer<Integer>>();

                        for (CellConsumer<Integer> c : currentConsumers)
                            c.next(next, nc -> nextConsumers.add(nc));

                        consumers = nextConsumers;
                        System.out.println("nextConsumers=" + nextConsumers);
                    }

                    private void propogateBuffer(int index, CellConsumer<Integer> consumer) {
                        if(index < buffer.size())
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
            public void visitIntLiteral(int value) {
                reduceTo(new Cell<Integer>(new JLabel("" + value)) {
                    @Override
                    public void consume(CellConsumer<Integer> consumer) {
                        consumer.next(value, nc -> nc.atEnd());
                    }
                });
            }

            @Override
            public void visitBinary(int operator, Expression lhs, Expression rhs) {
                Cell<Integer> lhsCell = (Cell<Integer>)expressionToView(lhs);
                Cell<Integer> rhsCell = (Cell<Integer>)expressionToView(rhs);

                JLabel label = new JLabel();

                reduceTo(new Cell<Integer>(label) {
                    private Number lhsValue;
                    private Number rhsValue;

                    private ArrayList<Integer> buffer = new ArrayList<Integer>();
                    private ArrayList<CellConsumer<Integer>> consumers = new ArrayList<CellConsumer<Integer>>();

                    {
                        lhsCell.consume(new CellConsumer.Infinite<Integer>() {
                            @Override
                            public void next(Integer value) {
                                lhsValue = value;
                                update();
                            }
                        });
                        rhsCell.consume(new CellConsumer.Infinite<Integer>() {
                            @Override
                            public void next(Integer value) {
                                rhsValue = value;
                                update();
                            }
                        });
                    }

                    private void update() {
                        if(lhsValue != null && rhsValue != null) {
                            System.out.println("update");

                            Integer next = null;

                            try {
                                switch (operator) {
                                    case ExpressionVisitor.BINARY_OPERATOR_ADD:
                                        next = lhsValue.intValue() + rhsValue.intValue();
                                        break;
                                    case ExpressionVisitor.BINARY_OPERATOR_SUB:
                                        next = lhsValue.intValue() - rhsValue.intValue();
                                        break;
                                    case ExpressionVisitor.BINARY_OPERATOR_MUL:
                                        next = lhsValue.intValue() * rhsValue.intValue();
                                        break;
                                    case ExpressionVisitor.BINARY_OPERATOR_DIV:
                                        next = lhsValue.intValue() / rhsValue.intValue();
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
                    public void consume(CellConsumer<Integer> consumer) {
                        propogateBuffer(0, consumer);
                    }

                    private void propogateNext(Integer next) {
                        System.out.println("propogateNext");
                        buffer.add(next);
                        ArrayList<CellConsumer<Integer>> currentConsumers = new ArrayList<CellConsumer<Integer>>(consumers);
                        ArrayList<CellConsumer<Integer>> nextConsumers = new ArrayList<CellConsumer<Integer>>();

                        for (CellConsumer<Integer> c : currentConsumers)
                            c.next(next, nc -> nextConsumers.add(nc));

                        consumers = nextConsumers;
                    }

                    private void propogateBuffer(int index, CellConsumer<Integer> consumer) {
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

package astava.samples.virela.view;

import astava.parse.CommonMatcher;
import astava.parse.Matcher;
import astava.parse.charsequence.CharSequenceCursor;
import astava.samples.virela.parser.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RelationSetView extends JPanel {
    private Hashtable<String, Cell<?>> relationSet = new Hashtable<>();
    private JPanel contentView = new JPanel();

    private ScheduledExecutorService parseExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> parseFuture;

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

                        java.util.List<Relation> newRelations = matcher.production().stream().collect(Collectors.toList());
                        Map<String, Expression> newRelationSet = matcher.production().stream().collect(Collectors.toMap(r -> r.getId(), r -> r.getValue()));

                        System.out.println("Ids: " + newRelations.stream().map(r -> r.getId()).collect(Collectors.toSet()));
                        System.out.println("Components count: " + contentView.getComponentCount());

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
                            JComponent relationValueView = cell.getView();
                            relationValueView.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                            JPanel relationView = new JPanel(new BorderLayout());
                            relationView.setBackground(Color.WHITE);
                            relationView.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                            relationView.add(topView, BorderLayout.WEST);
                            relationView.add(relationValueView);

                            relationSet.put(r.getId(), cell);
                            contentView.add(relationView);

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

                        // Deletions
                        relationSet.keySet().retainAll(newRelations.stream().map(r -> r.getId()).collect(Collectors.toSet()));

                        contentView.revalidate();
                        contentView.repaint();

                        System.out.println("Success, relation set size = " + newRelationSet.size());
                    } else {
                        System.out.println("Failure");
                    }
                }, 150, TimeUnit.MILLISECONDS);
            }
        });
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
                            }
                        });

                        Number currentValue = (Number)((JSpinner)getView()).getValue();
                        propogateNext(currentValue.intValue());
                    }

                    @Override
                    public void consume(CellConsumer<Integer> consumer) {
                        propogateBuffer(0, consumer);
                    }

                    private void propogateNext(Integer next) {
                        buffer.add(next);
                        ArrayList<CellConsumer<Integer>> currentConsumers = new ArrayList<CellConsumer<Integer>>(consumers);
                        ArrayList<CellConsumer<Integer>> nextConsumers = new ArrayList<CellConsumer<Integer>>();

                        for(CellConsumer<Integer> c : currentConsumers)
                            c.next(next, nc -> nextConsumers.add(nc));

                        consumers = nextConsumers;
                    }

                    private void propogateBuffer(int index, CellConsumer<Integer> consumer) {
                        if(index < buffer.size())
                            consumer.next(buffer.get(0), nc -> propogateBuffer(index + 1, nc));
                        else
                            consumers.add(consumer);
                    }

                    private void next(Integer value) {
                        buffer.add(value);
                    }
                });
            }

            @Override
            public void visitId(String id) {
                if(relationSet.containsKey(id)) {
                    JLabel label = new JLabel(id);

                    ((Cell<Object>)relationSet.get(id)).consume(new CellConsumer<Object>() {
                        @Override
                        public void next(Object value, Consumer<CellConsumer> remain) {
                            label.setText(value.toString());
                            remain.accept(this);
                        }

                        @Override
                        public void atEnd() {

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
                new String();
            }
        });
    }
}
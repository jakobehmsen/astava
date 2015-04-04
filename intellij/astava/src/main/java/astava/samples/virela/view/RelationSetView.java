package astava.samples.virela.view;

import astava.parse.CommonMatcher;
import astava.parse.Matcher;
import astava.parse.charsequence.CharSequenceCursor;
import astava.samples.virela.parser.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RelationSetView extends JPanel {
    private Hashtable<String, JComponent> relationSet = new Hashtable<>();
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
                            JComponent relationValueView = expressionToView(r.getValue());
                            relationValueView.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                            JPanel relationView = new JPanel(new BorderLayout());
                            relationView.setBackground(Color.WHITE);
                            relationView.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                            relationView.add(topView, BorderLayout.WEST);
                            relationView.add(relationValueView);

                            relationSet.put(r.getId(), relationView);
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

    private JComponent expressionToView(Expression expression) {
        return expression.reduce(new ExpressionReducer<JComponent>() {
            @Override
            public void visitIntStream() {
                reduceTo(new JSpinner());
            }

            @Override
            public void visitId(String id) {
                JLabel label = new JLabel(id);
                label.setFont(new Font(Font.MONOSPACED,  Font.BOLD, 14));
                reduceTo(label);
            }

            @Override
            public void visitIntLiteral(int value) {
                reduceTo(new JLabel("" + value));
            }

            @Override
            public void visitBinary(int operator, Expression lhs, Expression rhs) {
                new String();
            }
        });
    }
}

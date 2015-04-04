package astava.samples.virela.view;

import astava.parse.CommonMatcher;
import astava.parse.Matcher;
import astava.parse.charsequence.CharSequenceCursor;
import astava.samples.virela.parser.Expression;
import astava.samples.virela.parser.Relation;
import astava.samples.virela.parser.RelationParser;

import javax.swing.*;
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
                            if (!relationSet.containsKey(r.getId())) {
                                // Insertion
                                // View should be dependent on expression:
                                // E.g. int stream should be numeric up down
                                JTextArea relationView = new JTextArea(r.getId());
                                relationSet.put(r.getId(), relationView);
                                contentView.add(relationView);
                            }
                            {
                                // Update
                                JComponent relationView = relationSet.get(r.getId());
                                contentView.add(relationView);
                            }
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
}

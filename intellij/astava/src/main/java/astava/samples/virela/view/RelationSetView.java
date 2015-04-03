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
import java.util.stream.Collectors;

public class RelationSetView extends JPanel {
    private Hashtable<String, JComponent> relationSet = new Hashtable<>();

    public RelationSetView() {
        setLayout(new BorderLayout());

        JPanel contentView = new JPanel();
        add(contentView, BorderLayout.CENTER);

        JTextPane scriptView = new JTextPane();
        scriptView.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String source = scriptView.getText();

                    relationSet.values().forEach(x -> remove(x));

                    RelationParser parser = new RelationParser();
                    Matcher<Character, Relation> matcher = parser.parseInit(new CharSequenceCursor(source), (p, c) -> new CommonMatcher<>());

                    if(matcher.isMatch()) {
                        //matcher.production().stream().forEach(x -> print(x));
                        Map<String, Expression> newRelationSet = matcher.production().stream().collect(Collectors.toMap(r -> r.getId(), r -> r.getValue()));

                        newRelationSet.entrySet().stream().forEach(x -> {
                            if (!relationSet.containsKey(x.getKey())) {
                                // Insertion
                                JTextArea relationView = new JTextArea(x.getKey());
                                relationSet.put(x.getKey(), relationView);
                                contentView.add(relationView);
                            } {
                                // Update
                                JComponent relationView = relationSet.get(x.getKey());
                                contentView.add(relationView);
                            }
                        });

                        // Deletions
                        relationSet.entrySet().stream().filter(x -> !newRelationSet.containsKey(x.getKey())).forEach(x -> {
                            JComponent relationView = relationSet.get(x.getKey());
                            contentView.remove(relationView);
                        });

                        contentView.revalidate();
                        contentView.repaint();

                        System.out.print("Success");
                    } else {
                        System.out.print("Failure");
                    }
                }
            }
        });
        scriptView.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        add(scriptView, BorderLayout.SOUTH);
    }
}

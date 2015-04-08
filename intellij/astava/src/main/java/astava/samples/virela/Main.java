package astava.samples.virela;

import astava.samples.virela.parser.Expression;
import astava.samples.virela.parser.ExpressionVisitor;
import astava.samples.virela.view.RelationSetView;

import javax.swing.*;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        /*String strInput =
            "sat = 5.5"
            ;

        RelationParser parser = new RelationParser();
        Matcher<Character, Relation> matcher = parser.parseInit(new CharSequenceCursor(strInput), (p, c) -> new CommonMatcher<>());

        if(matcher.isMatch()) {
            matcher.production().stream().forEach(x -> print(x));

            System.out.print("Success");
        } else {
            System.out.print("Failure");
        }

        if(1 != 2)
            return;*/


        JFrame frame = new JFrame();

        frame.getContentPane().add(new RelationSetView());

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        /*
        String strInput =
            "sat = int\n" +
            "sbt = sat / 2\n" +
            //"sat = 3454\n" +
            //"sbt = sat\n" +
            ""
            ;

        RelationParser parser = new RelationParser();
        Matcher<Character, Relation> matcher = parser.parseInit(new CharSequenceCursor(strInput), (p, c) -> new CommonMatcher<>());

        if(matcher.isMatch()) {
            matcher.production().stream().forEach(x -> print(x));

            System.out.print("Success");
        } else {
            System.out.print("Failure");
        }
        */
    }
}

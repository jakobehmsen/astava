package astava.samples.virela;

import astava.parse.CommonMatcher;
import astava.parse.Matcher;
import astava.parse.charsequence.CharSequenceCursor;

public class Main {
    public static void main(String[] args) {
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
    }

    private static void print(Relation relation) {
        System.out.print(relation.getId() + " = ");
        relation.getValue().accept(new ExpressionVisitor() {
            @Override
            public void visitIntStream() {
                System.out.print("int");
            }

            @Override
            public void visitId(String id) {
                System.out.print(id);
            }

            @Override
            public void visitIntLiteral(int value) {
                System.out.print(value);
            }

            @Override
            public void visitBinary(int operator, Expression lhs, Expression rhs) {
                lhs.accept(this);
                System.out.print(" ");
                switch (operator) {
                    case ExpressionVisitor.BINARY_OPERATOR_MUL:
                        System.out.print("*");
                        break;
                    case ExpressionVisitor.BINARY_OPERATOR_DIV:
                        System.out.print("/");
                        break;
                }
                System.out.print(" ");
                rhs.accept(this);
            }
        });
        System.out.println();
    }
}

package astava.parse3;

import java.util.function.Predicate;

public class Parse {
    public static Parser<Character> isChar(char ch) {
        //return isPeek(value -> value == ch);

        return isPeek(new Predicate<Character>() {
            @Override
            public boolean test(Character value) {
                return value == ch;
            }

            @Override
            public String toString() {
                return "Is char '" + ch + "'.";
            }
        });
    }

    public static <T> Parser<T> isPeek(Predicate<T> predicate) {
        return new Parser<T>() {
            @Override
            public void parse(Input<T> input, Matcher matcher) {
                matcher.visitPreInput(input);
                if(!input.atEnd() && predicate.test(input.peek())) {
                    matcher.visitSuccess();
                    matcher.visitCaptured(input.peek());
                    input.consume();
                } else
                    matcher.visitFailure();
                matcher.visitPostInput(input);
            }

            @Override
            public String toString() {
                return predicate.toString();
            }
        };

        /*return (input, matcher) -> {
            if(!input.atEnd() && predicate.test(input.peek())) {
                matcher.visitSuccess();
                matcher.visitCaptured(input.peek());
                input.consume();
            } else
                matcher.visitFailure();
        };*/
    }

    public static <T> Parser<T> sequence(Parser<T>... parsers) {
        return (input, matcher) -> {
            matcher.visitPreInput(input);
            for(Parser<T> parser: parsers) {
                Matcher<T> elementMatcher = matcher.beginVisit(parser);
                parser.parse(input, elementMatcher);
                if(!elementMatcher.endVisit()) {
                    matcher.visitFailure();
                    matcher.visitPostInput(input);
                    return;
                }
            }

            matcher.visitSuccess();
            matcher.visitPostInput(input);
        };
    }
}

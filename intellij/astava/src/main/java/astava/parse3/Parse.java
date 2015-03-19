package astava.parse3;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Parse {
    public static Parser<Character> isChar(char ch) {
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
            public <R extends Matcher<T>> R parse(Input<T> input, R matcher) {
                if(!input.atEnd() && predicate.test(input.peek())) {
                    input.consume();
                    matcher.visitSuccess();
                } else
                    matcher.visitFailure();
                return matcher;
            }

            @Override
            public String toString() {
                return predicate.toString();
            }
        };
    }

    public static <T> Parser<T> sequence(Parser<T>... parsers) {
        return new Parser<T>() {
            @Override
            public <R extends Matcher<T>> R parse(Input<T> input, R matcher) {
                for(Parser<T> parser: parsers) {
                    Matcher<T> elementMatcher = matcher.beginVisit(parser, input);
                    parser.parse(input, elementMatcher);
                    if(!elementMatcher.isMatch()) {
                        matcher.visitFailure();
                        return matcher;
                    }
                }

                matcher.visitSuccess();
                return matcher;
            }

            @Override
            public String toString() {
                return Arrays.asList(parsers).stream().map(a -> a.toString()).collect(Collectors.joining(", "));
            }
        };
    }

    public static <T> Parser<T> decision(Parser<T>... alternatives) {
        return new Parser<T>() {
            @Override
            public <R extends Matcher<T>> R parse(Input<T> input, R matcher) {
                for(Parser<T> alternative: alternatives) {
                    Matcher<T> alternativeMatcher = matcher.beginVisit(alternative, input);
                    alternative.parse(input, alternativeMatcher);
                    if(alternativeMatcher.isMatch()) {
                        matcher.visitSuccess();
                        return matcher;
                    }
                }

                matcher.visitFailure();
                return matcher;
            }

            @Override
            public Parser<T> or(Parser<T> other) {
                Parser<T>[] newAlternatives = (Parser<T>[])new Parser[alternatives.length + 1];
                System.arraycopy(alternatives, 0, newAlternatives, 0, alternatives.length);
                newAlternatives[newAlternatives.length - 1] = other;
                return decision(newAlternatives);
            }

            @Override
            public String toString() {
                return Arrays.asList(alternatives).stream().map(a -> a.toString()).collect(Collectors.joining(" | "));
            }
        };
    }

    public static <T> Parser<T> ref(Supplier<Parser<T>> parserSupplier) {
        return new Parser<T>() {
            @Override
            public <R extends Matcher<T>> R parse(Input<T> input, R matcher) {
                Parser<T> parser = parserSupplier.get();
                R refMatcher = (R)matcher.beginVisit(parser, input);
                parser.parse(input, refMatcher);
                refMatcher.propogate(matcher);
                return matcher;
            }

            @Override
            public String toString() {
                return "Ref";
            }
        };
    }
}

package astava.parse3;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Parse {
    public static class IsCharSequence implements Parser<Character> {
        private String chars;

        public IsCharSequence(String chars) {
            this.chars = chars;
        }

        @Override
        public <R extends Matcher<Character>> R parse(Input<Character> input, R matcher) {
            for(int i = 0; i < chars.length(); i++) {
                if(!input.atEnd() && (char)input.peek() == chars.charAt(i))
                    input.consume();
                else {
                    matcher.visitFailure();
                    return matcher;
                }
            }

            matcher.visitSuccess();
            return matcher;
        }

        @Override
        public Parser<Character> then(Parser<Character> next) {
            if(next instanceof IsCharSequence)
                return new IsCharSequence(this.chars + ((IsCharSequence)next).chars);

            return Parser.super.then(next);
        }

        @Override
        public String toString() {
            return "\"" + chars + "\"";
        }
    };

    public static Parser<Character> isChars(String chars) {
        return new IsCharSequence(chars);
    }

    public static class IsChar implements Parser<Character> {
        private char ch;

        public IsChar(char ch) {
            this.ch = ch;
        }

        @Override
        public <R extends Matcher<Character>> R parse(Input<Character> input, R matcher) {
            if(!input.atEnd() && (char)input.peek() == ch) {
                input.consume();
                matcher.visitSuccess();
            } else
                matcher.visitFailure();
            return matcher;
        }

        @Override
        public Parser<Character> then(Parser<Character> next) {
            if(next instanceof IsChar)
                return new IsCharSequence("" + this.ch + ((IsChar)next).ch);

            return Parser.super.then(next);
        }

        @Override
        public String toString() {
            return "'" + ch + "'";
        }
    }

    public static Parser<Character> isChar(char ch) {
        return new IsChar(ch);
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
            public Parser<T> then(Parser<T> next) {
                Parser<T>[] newAlternatives = (Parser<T>[])new Parser[parsers.length + 1];
                System.arraycopy(parsers, 0, newAlternatives, 0, parsers.length);
                newAlternatives[newAlternatives.length - 1] = next;
                return sequence(newAlternatives);
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
}

package astava.parse3;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Parse {
    public static <TIn, TOut> Parser<TIn, TOut> consume() {
        return new Parser<TIn, TOut>() {
            @Override
            public void parse(Input<TIn> input, Matcher<TIn, TOut> matcher) {
                if(!input.atEnd()) {
                    input.consume();
                    matcher.visitSuccess();
                } else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return "++";
            }
        };
    }

    public static <TIn> Parser<TIn, TIn> copy() {
        return new Parser<TIn, TIn>() {
            @Override
            public void parse(Input<TIn> input, Matcher<TIn, TIn> matcher) {
                if(!input.atEnd()) {
                    TIn value = input.peek();
                    matcher.put(value);
                    matcher.visitSuccess();
                } else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return "^";
            }
        };
    }

    public static class IsCharSequence<TOut> implements Parser<Character, TOut> {
        private String chars;

        public IsCharSequence(String chars) {
            this.chars = chars;
        }

        @Override
        public void parse(Input<Character> input, Matcher<Character, TOut> matcher) {
            for(int i = 0; i < chars.length(); i++) {
                if(!input.atEnd() && (char)input.peek() == chars.charAt(i))
                    input.consume();
                else {
                    matcher.visitFailure();
                }
            }

            matcher.visitSuccess();
        }

        @Override
        public Parser<Character, TOut> then(Parser<Character, TOut> next) {
            if(next instanceof IsCharSequence)
                return new IsCharSequence(this.chars + ((IsCharSequence)next).chars);

            return Parser.super.then(next);
        }

        @Override
        public String toString() {
            return "\"" + chars + "\"";
        }
    };

    public static <TOut> Parser<Character, TOut> isChars(String chars) {
        return new IsCharSequence(chars);
    }

    public static class IsChar<TOut> implements Parser<Character, TOut> {
        private char ch;

        public IsChar(char ch) {
            this.ch = ch;
        }

        @Override
        public void parse(Input<Character> input, Matcher<Character, TOut> matcher) {
            if(!input.atEnd() && (char)input.peek() == ch) {
                matcher.visitSuccess();
            } else
                matcher.visitFailure();
        }

        @Override
        public Parser<Character, TOut> then(Parser<Character, TOut> next) {
            if(next instanceof IsChar)
                return new IsCharSequence("" + this.ch + ((IsChar)next).ch);

            return Parser.super.then(next);
        }

        @Override
        public String toString() {
            return "'" + ch + "'";
        }
    }

    public static <TOut> Parser<Character, TOut> isChar(char ch) {
        return new IsChar(ch);
    }

    public static <TIn, TOut> Parser<TIn, TOut> sequence(Parser<TIn, TOut>... parsers) {
        return new Parser<TIn, TOut>() {
            @Override
            public void parse(Input<TIn> input, Matcher<TIn, TOut> matcher) {
                for(Parser<TIn, TOut> parser: parsers) {
                    Matcher<TIn, TOut> elementMatcher = matcher.beginVisit(parser, input);
                    parser.parse(input, elementMatcher);
                    if(!elementMatcher.isMatch()) {
                        matcher.visitFailure();
                        return;
                    }
                    elementMatcher.propagateOutput(matcher);
                }

                matcher.visitSuccess();
            }

            @Override
            public Parser<TIn, TOut> then(Parser<TIn, TOut> next) {
                Parser<TIn, TOut>[] newAlternatives = (Parser<TIn, TOut>[])new Parser[parsers.length + 1];
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

    public static <TIn, TOut> Parser<TIn, TOut> decision(Parser<TIn, TOut>... alternatives) {
        return new Parser<TIn, TOut>() {
            @Override
            public void parse(Input<TIn> input, Matcher<TIn, TOut> matcher) {
                for(Parser<TIn, TOut> alternative: alternatives) {
                    Matcher<TIn, TOut> alternativeMatcher = matcher.beginVisit(alternative, input);
                    alternative.parse(input, alternativeMatcher);
                    if(alternativeMatcher.isMatch()) {
                        alternativeMatcher.propagateOutput(matcher);
                        matcher.visitSuccess();
                        return;
                    }
                }

                matcher.visitFailure();
            }

            @Override
            public Parser<TIn, TOut> or(Parser<TIn, TOut> other) {
                Parser<TIn, TOut>[] newAlternatives = (Parser<TIn, TOut>[])new Parser[alternatives.length + 1];
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

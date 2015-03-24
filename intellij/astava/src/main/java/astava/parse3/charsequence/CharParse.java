package astava.parse3.charsequence;

import astava.parse3.*;

import java.util.function.Function;

public class CharParse {
    public static class IsCharSequence<TOut> implements Parser<Character, TOut> {
        private String chars;

        public IsCharSequence(String chars) {
            this.chars = chars;
        }

        @Override
        public void parse(Cursor<Character> cursor, Matcher<Character, TOut> matcher) {
            for(int i = 0; i < chars.length(); i++) {
                if(!cursor.atEnd() && (char) cursor.peek() == chars.charAt(i))
                    cursor.consume();
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

    public static abstract class CharPredicate<TOut> implements LeafParser<Character, TOut> {
        @Override
        public void parse(Cursor<Character> cursor, Matcher<Character, TOut> matcher) {
            if(!cursor.atEnd() && test((char) cursor.peek())) {
                matcher.visitSuccess();
            } else
                matcher.visitFailure();
        }

        protected abstract boolean test(char ch);
    }

    public static class IsChar<TOut> extends CharPredicate<TOut> {
        private char ch;

        public IsChar(char ch) {
            this.ch = ch;
        }

        @Override
        protected boolean test(char ch) {
            return this.ch == ch;
        }

        @Override
        public Parser<Character, TOut> then(Parser<Character, TOut> next) {
            if(next instanceof IsChar)
                return new IsCharSequence("" + this.ch + ((IsChar)next).ch);

            return super.then(next);
        }

        @Override
        public String toString() {
            return "'" + ch + "'";
        }
    }



    public static <TOut> LeafParser<Character, TOut> isChar(char ch) {
        return new IsChar(ch);
    }

    public static <TOut> LeafParser<Character, TOut> isLetter() {
        return new CharPredicate<TOut>() {
            @Override
            protected boolean test(char ch) {
                return Character.isLetter(ch);
            }

            @Override
            public String toString() {
                return "<is-letter>";
            }
        };
    }

    public static <TOut> LeafParser<Character, TOut> isWhitespace() {
        return new CharPredicate<TOut>() {
            @Override
            protected boolean test(char ch) {
                return Character.isWhitespace(ch);
            }

            @Override
            public String toString() {
                return "<is-whitespace>";
            }
        };
    }

    public static <TOut> LeafParser<Character, TOut> isDigit() {
        return new CharPredicate<TOut>() {
            @Override
            protected boolean test(char ch) {
                return Character.isDigit(ch);
            }

            @Override
            public String toString() {
                return "<is-digit>";
            }
        };
    }
}

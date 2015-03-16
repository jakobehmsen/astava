package astava.parse2;

public abstract class CharPredicate<T> implements Parser<Character, Character, T> {
    public static CharPredicate<String> is(char ch) {
        return new CharPredicate<String>() {
            @Override
            protected boolean accepts(Source<Character> source) {
                return ch == source.getChar();
            }

            @Override
            protected String getFailureValue() {
                return "Expected '" + ch + "'.";
            }
        };
    }

    public static CharPredicate<String> isLetter() {
        return new CharPredicate<String>() {
            @Override
            protected boolean accepts(Source<Character> source) {
                return Character.isLetter(source.getChar());
            }

            @Override
            protected String getFailureValue() {
                return "Expected letter.";
            }
        };
    }

    public static CharPredicate<String> isDigit() {
        return new CharPredicate<String>() {
            @Override
            protected boolean accepts(Source<Character> source) {
                return Character.isDigit(source.getChar());
            }

            @Override
            protected String getFailureValue() {
                return "Expected digit.";
            }
        };
    }

    public static CharPredicate<String> isWhitespace() {
        return new CharPredicate<String>() {
            @Override
            protected boolean accepts(Source<Character> source) {
                return Character.isWhitespace(source.getChar());
            }

            @Override
            protected String getFailureValue() {
                return "Expected whitespace.";
            }
        };
    }

    @Override
    public ParseResult<Character, Character, T> parse(ParseContext<T> ctx, Source<Character> source) {
        if(!source.atEnd() && accepts(source))
            return ctx.success(source.rest(), source.getChar());

        return ctx.failure(source, getFailureValue());
    }

    protected abstract boolean accepts(Source<Character> source);
    protected abstract T getFailureValue();
}

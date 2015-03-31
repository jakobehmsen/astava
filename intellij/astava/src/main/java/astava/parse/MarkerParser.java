package astava.parse;

public class MarkerParser<TIn, TOut> implements Parser<TIn, TOut> {
    private Parser<TIn, TOut> parser;

    public MarkerParser(Parser<TIn, TOut> parser) {
        this.parser = parser;
    }

    @Override
    public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
        parser.parse(cursor, matcher);
    }

    @Override
    public String toString() {
        return parser.toString();
    }
}

package astava.parse3;

public class MarkerParser<TIn, TOut> implements Parser<TIn, TOut> {
    private Parser<TIn, TOut> parser;

    public MarkerParser(Parser<TIn, TOut> parser) {
        this.parser = parser;
    }

    @Override
    public void parse(Input<TIn> input, Matcher<TIn, TOut> matcher) {
        parser.parse(input, matcher);
    }
}

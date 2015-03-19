package astava.parse3;

public abstract class DelegateParser<T> implements Parser<T> {
    private Parser<T> parser;

    public DelegateParser() {
        parser = createParser();
    }

    protected abstract Parser<T> createParser();

    @Override
    public <R extends Matcher<T>> R parse(Input<T> input, R matcher) {
        return parser.parse(input, matcher);
    }

    @Override
    public String toString() {
        return parser.toString();
    }
}

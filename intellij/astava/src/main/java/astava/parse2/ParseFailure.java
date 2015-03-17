package astava.parse2;

public class ParseFailure<T, Success, Failure> implements ParseResult<T, Success, Failure> {
    private ParseContext<Failure> parent;
    private Source<T> source;
    private Failure value;

    public ParseFailure(ParseContext<Failure> parent, Source<T> source, Failure value) {
        this.parent = parent;
        this.source = source;
        this.value = value;
    }

    @Override
    public ParseContext<Failure> getParent() {
        return parent;
    }

    @Override
    public Source<T> getSource() {
        return source;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Success getValueIfSuccess() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Failure getValueIfFailure() {
        return value;
    }

    @Override
    public <T, Success> ParseResult<T, Success, Failure> success(Source<T> source, Success value) {
        return new ParseSuccess<T, Success, Failure>(this, source, value);
    }

    @Override
    public <T, Success> ParseResult<T, Success, Failure> failure(Source<T> source, Failure value) {
        return new ParseFailure<T, Success, Failure>(this, source, value);
    }
}

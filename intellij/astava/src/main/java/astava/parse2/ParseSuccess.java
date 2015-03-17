package astava.parse2;

public class ParseSuccess<T, Success, Failure> implements ParseResult<T, Success, Failure> {
    private ParseContext<Failure> parent;
    private Source<T> source;
    private Success value;

    public ParseSuccess(ParseContext<Failure> parent, Source<T> source, Success value) {
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
        return true;
    }

    @Override
    public Success getValueIfSuccess() {
        return value;
    }

    @Override
    public Failure getValueIfFailure() {
        throw new UnsupportedOperationException();
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

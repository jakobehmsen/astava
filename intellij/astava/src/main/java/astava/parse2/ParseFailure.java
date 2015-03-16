package astava.parse2;

public class ParseFailure<T, Success, Failure> implements ParseResult<T, Success, Failure> {
    private ParseResult<?, ?, Failure> parent;
    private Source<T> source;
    private Failure value;
    private boolean isTracked;

    public ParseFailure(ParseResult<?, ?, Failure> parent, Source<T> source, Failure value, boolean isTracked) {
        this.parent = parent;
        this.source = source;
        this.value = value;
        this.isTracked = isTracked;
    }

    @Override
    public ParseResult<?, ?, Failure> getParent() {
        return parent;
    }

    @Override
    public Source<T> getSource() {
        return source;
    }

    @Override
    public boolean isSuccess() {
        return isTracked;
    }

    @Override
    public Success getValueIfSuccess() {
        if(isSuccess())
            return (Success)parent.getValueIfSuccess();

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
    public <T, Success> ParseResult<T, Success, Failure> trackFailure(Source<T> source, Failure value) {
        return new ParseFailure<T, Success, Failure>(this, source, value, true);
    }

    @Override
    public <T, Success> ParseResult<T, Success, Failure> failure(Source<T> source, Failure value) {
        return new ParseFailure<T, Success, Failure>(this, source, value, false);
    }
}

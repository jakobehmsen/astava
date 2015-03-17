package astava.parse2;

public class ParseFrameBuilder<T, Success, Failure> implements ParseResult<T, Success, Failure> {
    private ParseContext<Failure> parent;
    private ParseResult<T, Success, Failure> result;
    private Object description;

    public ParseFrameBuilder(ParseContext<Failure> parent, ParseResult<T, Success, Failure> result, Object description) {
        this.parent = parent;
        this.result = result;
        this.description = description;
    }

    @Override
    public ParseContext<Failure> getParent() {
        return parent;
    }

    @Override
    public Source<T> getSource() {
        return result.getSource();
    }

    @Override
    public boolean isSuccess() {
        return result.isSuccess();
    }

    @Override
    public Success getValueIfSuccess() {
        return result.getValueIfSuccess();
    }

    @Override
    public Failure getValueIfFailure() {
        return result.getValueIfFailure();
    }

    public ParseResult<T, Success, Failure> getResult() {
        return result;
    }

    public Object getDescription() {
        return description;
    }

    @Override
    public <T, Success> ParseResult<T, Success, Failure> success(Source<T> source, Success value) {
        //return new ParseSuccess<>(this, source, value);
        //ParseResult<T, Success, Failure> success = result != null ? result.success(source, value) : parent.success(source, value);
        ParseResult<T, Success, Failure> success = result != null ? result.success(source, value) : new ParseSuccess<>(null, source, value);
        return new ParseFrameBuilder<>(parent, success, description);
    }

    @Override
    public <T, Success> ParseResult<T, Success, Failure> failure(Source<T> source, Failure value) {
        //return new ParseFailure<>(this, source, value);
        //ParseResult<T, Success, Failure> failure = result != null ? result.failure(source, value) : parent.failure(source, value);
        ParseResult<T, Success, Failure> failure = result != null ? result.failure(source, value) : new ParseFailure<>(null, source, value);
        return new ParseFrameBuilder<>(parent, failure, description);
    }

    public ParseResult<T, Success, Failure> build() {
        return new ParseFrame<>(parent, result, description);
    }
}

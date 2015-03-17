package astava.parse2;

/**
 * Created by jakob on 17-03-15.
 */
public class ParseFrame<T, Success, Failure> implements ParseResult<T, Success, Failure> {
    private ParseContext<Failure> parent;
    private ParseResult<T, Success, Failure> result;
    private Object description;

    public ParseFrame(ParseContext<Failure> parent, ParseResult<T, Success, Failure> result, Object description) {
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
        return new ParseSuccess<>(this, source, value);
    }

    @Override
    public <T, Success> ParseResult<T, Success, Failure> failure(Source<T> source, Failure value) {
        return new ParseFailure<>(this, source, value);
    }
}

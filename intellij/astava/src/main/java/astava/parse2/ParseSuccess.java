package astava.parse2;

public class ParseSuccess<T, R, S> implements ParseResult<T,R,S> {
    private Source source;
    private R value;

    public ParseSuccess(Source source, R value) {
        this.source = source;
        this.value = value;
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
    public R getValueIfSuccess() {
        return value;
    }

    @Override
    public S getValueIfFailure() {
        throw new UnsupportedOperationException();
    }
}

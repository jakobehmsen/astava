package astava.parse2;

public class ParseFailure<T, R, S> implements ParseResult<T,R,S> {
    private Source source;
    private S value;

    public ParseFailure(Source source, S value) {
        this.source = source;
        this.value = value;
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
    public R getValueIfSuccess() {
        throw new UnsupportedOperationException();
    }

    @Override
    public S getValueIfFailure() {
        return value;
    }
}

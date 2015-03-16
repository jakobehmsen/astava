package astava.parse2;

public interface ParseResult<T, Success, Failure> extends ParseContext<Failure> {
    ParseResult<?, ?, Failure> getParent();
    Source<T> getSource();

    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    Success getValueIfSuccess();
    Failure getValueIfFailure();
}

package astava.parse2;

public interface ParseResult<T, R, S> {
    Source<T> getSource();

    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    R getValueIfSuccess();
    S getValueIfFailure();
}

package astava.parse3;

public interface CursorStateFactory<T, R extends Cursor<T>> {
    void consume(T value);
    CursorState getState(R cursor);
    default void consume(char ch) {
        consume((Character)ch);
    }
}

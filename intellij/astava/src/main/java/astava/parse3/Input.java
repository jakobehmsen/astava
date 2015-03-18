package astava.parse3;

public interface Input<T> {
    T peek();
    void consume();
    boolean atEnd();
}

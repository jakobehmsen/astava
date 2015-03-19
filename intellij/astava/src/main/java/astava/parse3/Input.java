package astava.parse3;

public interface Input<T> {
    Position<T> position();
    Input<T> interval(Position<T> start, Position<T> end);
    T peek();
    void consume();
    boolean atEnd();

    void setPosition(Position<T> position);
}

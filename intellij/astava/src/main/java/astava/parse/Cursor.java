package astava.parse;

public interface Cursor<T>  {

    //Position<T> position();
    //Cursor<T> interval(Position<T> start, Position<T> end);
    T peek();
    void consume();
    boolean atEnd();
    CursorState state();

    //void setPosition(Position<T> position);
    /*default Stream<T> stream() {
        return new Stream<T>() {
        }
    }*/
}

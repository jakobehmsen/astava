package astava.parse3;

import java.util.Comparator;

public interface Cursor<T>  {
    public interface State extends Comparable<State> {
        void restore();
    }

    //Position<T> position();
    //Cursor<T> interval(Position<T> start, Position<T> end);
    T peek();
    void consume();
    boolean atEnd();
    State state();

    //void setPosition(Position<T> position);
    /*default Stream<T> stream() {
        return new Stream<T>() {
        }
    }*/
}

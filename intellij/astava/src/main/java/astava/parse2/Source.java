package astava.parse2;

public interface Source<T> {
    T get();
    default char getChar() {
        throw new UnsupportedOperationException();
    }
    Source<T> rest();
    boolean atEnd();
}

package astava.java.agent;

public interface TriFunction<T, R, S, V> {
    V apply(T t, R r, S s);
}

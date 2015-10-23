package astava.java.agent;

public interface BiFunctionException<T, R, S> {
    S apply(T t, R r) throws Exception;
}

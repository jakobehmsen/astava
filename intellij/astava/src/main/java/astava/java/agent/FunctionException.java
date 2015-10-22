package astava.java.agent;

public interface FunctionException<T, R> {
    R apply(T r) throws Exception;
}

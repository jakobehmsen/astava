package astava.java.agent.sample.EasyObservable;

public interface Observer {
    void changingField(Object target, String name);
    void changedField(Object target, String name);
    void enter(Object target, String methodName);
    void leave(Object target, String methodName);
}

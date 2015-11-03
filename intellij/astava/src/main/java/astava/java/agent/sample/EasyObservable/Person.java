package astava.java.agent.sample.EasyObservable;

@Support(Observable.class)
public class Person {
    private String name;

    public void setName() {
        this.name = "Some value";
    }
}

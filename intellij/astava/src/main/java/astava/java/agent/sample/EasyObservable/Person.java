package astava.java.agent.sample.EasyObservable;

@Support(Observable.class)
public class Person {
    private String name;

    public static Object v = null;

    public void setName(String newName) {
        if(this.name != v && !this.name.equals(newName)) {
            this.name = newName;
        }
    }
}

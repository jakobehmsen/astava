package astava.java.agent.sample;

import java.io.Serializable;

@MyAnnotation(value = 333, extra = "bla")
public class MyClass extends MyOtherClass {
    public String someField;
    public String someOtherField;

    public MyClass() {

    }

    public MyClass(String dummy) {

    }

    @Override
    public String toString() {
        return super.toString();
    }

    public boolean someMethod(int arg0, int arg1) {
        return false;
    }
}

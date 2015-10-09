package astava.java.agent.sample;

/**
 * Created by jakob on 09-10-15.
 */
public class Main {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        MyClass mc = new MyClass();
        //Object mc = Class.forName("astava.java.agent.sample.MyClass").newInstance();
        mc.toString();
    }
}

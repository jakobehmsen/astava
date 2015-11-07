package astava.java.agent.sample;

import java.io.Serializable;
import java.util.EmptyStackException;

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

    public boolean someOtherMethod(int arg0, int arg1, @MyNotNullAnnotation String str) {
        return false;
    }

    @MyNotNullAnnotation
    private boolean someOtherMethod2(String str1, String str2) {
        return false;
    }

    //private int i;

    private boolean b2;
    private boolean b3;
    private boolean b4;

    @MyNotNullAnnotation
    public boolean someOtherMethod3(String str1, String str2) {
        b2 = true;
        //i++;
        System.out.println("Within someOtherMethod3");
        //throw new RuntimeException("ASFSD");
        //throw new NullPointerException("ASFSD");
        //throw new IllegalArgumentException();
        //throw new EmptyStackException();
        //return i % 2 == 0;
        return true;
    }

    /*@MyNotNullAnnotation
    public boolean someOtherMethod5(String str1, String str2) {
        try {

        } catch(Exception e) {

        }
    }*/

    @MyNotNullAnnotation
    public void someOtherMethod4(String str1, String str2) {
        System.out.println("Within someOtherMethod4");
        //throw new RuntimeException("ASFSD");
    }

    public boolean someOtherMethod5(String str1, String str2) {
        try {
            System.out.println("Within someOtherMethod4");
            //throw new RuntimeException("ASFSD");
            return false;
        } catch(RuntimeException e) {
            System.out.println("Exc1 someOtherMethod4");
            return false;
        } catch(Exception e) {
            System.out.println("Exc2 someOtherMethod4");
            return false;
        }  finally {
            System.out.println("Finally");
        }
    }

    public int myMethod() {
        if(b2) {
            return 1;
        } else
            return 2;
    }

    public int myMethod2() {
        int i;

        if(b2) {
            i = 1;
        } else
            i = 2;

        return i;
    }

    public int myMethod3() {
        int i = 0;

        if(b2) {
            i = 1;
        }

        return i;
    }

    public int myMethodAnd() {
        int i = 0;

        if(b2 &&
            b3 && b4) {
            i = 1;
        } else {
            i = 2;
        }

        return i;
    }

    public int myMethodAnd2() {
        int i = 0;

        if(b2) {
            if(b3)
                if(b4)
                    i = 1;
        } else {
            i = 2;
        }

        return i;
    }

    public int myMethodOr() {
        int i = 0;

        if(b2 || b3 || b4) {
            i = 1;
        } else {
            i = 2;
        }

        return i;
    }

    public int myMethodAndOr() {
        int i = 0;

        if(b2 && b3 || b4) {
            i = 1;
        } else {
            i = 2;
        }

        return i;
    }

    public int myMethodOrAnd() {
        int i = 0;

        if(b2 || b3 && b4) {
            i = 1;
        } else {
            i = 2;
        }

        return i;
    }
}

package astava.jawico;

public class Main {
    /*
    Jawico: Java with continuations
    */

    public interface Continuation0 {
        void resume();
    }

    public interface Continuation1<T> {
        void resume(T val);
    }

    private static ThreadLocal<Boolean> running = new InheritableThreadLocal<>();
    private static ThreadLocal<Continuation0> next = new InheritableThreadLocal<>();

    private static void schedule(Continuation0 task) {
        next.set(task);

        if(!running.get()) {
            running.set(true);
            do {
                task.resume();
                task = next.get();
                next.set(null);
            } while(task != null);
            running.set(false);
        }
    }

    public static void main(String[] args) {
        running.set(false);
        //schedule(() -> myFunction());
        schedule(() -> myFunction1());
    }

    /*
    Example:

    int i = 0;
    frame c = here;
    i++;
    System.out.println("Hi" + 1);
    if(i < 10)
        c.resume();
    */

    /*public static void func(int state, int i) {
        switch (state) {
            case 0:
                break l0;
            case 1:
                break l1;
        }

        l0:
        i = 0;

        while(i < 10) {
            if(i < 5) {
                consume(() -> schedule(() -> func(1, i))); // Call with current continuation
                l1:
            }

            i++;
        }
    }*/

    private static void consume(Continuation0 c) {
        c.resume();
    }



    public static class Here_myFunction1_0_Frame implements Continuation0 {
        public int i;
        public Here_myFunction1_0_Frame c;

        @Override
        public void resume() {
            schedule(() -> myFunction1_0(i, c));
        }
    }

    public static void myFunction1() {
        int i = 0;
        Here_myFunction1_0_Frame c = new Here_myFunction1_0_Frame();
        c.i = i;
        c.c = c;
        myFunction1_0(i, c);
    }

    public static void myFunction1_0(int i, Here_myFunction1_0_Frame c) {
        System.out.println("Hi" + i);

        if(i < 10) {
            i++;
            c.i = i;
            c.resume();
        }
    }

    public static class Here_myFunction_0 {
        public void resume(int i, Here_myFunction_0 c) {
            myFunction_0(i, c);
        }
    }

    public static void myFunction() {
        int i = 0;
        myFunction_0(i, new Here_myFunction_0());
    }

    public static void myFunction_0(int i, Here_myFunction_0 c) {
        System.out.println("Hi" + i);

        if(i < 10) {
            i++;
            int c_i = i;
            schedule(() -> c.resume(c_i, c));
        }
    }
}

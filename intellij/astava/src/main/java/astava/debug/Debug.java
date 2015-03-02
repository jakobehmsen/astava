package astava.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Debug {
    public static final int LEVEL_HIGH = 1;
    public static final int LEVEL_LOW = 0;

    public static final int LEVEL = LEVEL_LOW;
    //public static final int LEVEL = LEVEL_HIGH;

    private static final PrintStream NULL_PRINT_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException { }
    });

    public static PrintStream getPrintStream(int level) {
        if(level <= LEVEL)
            return System.out;

        return NULL_PRINT_STREAM;
    }
}

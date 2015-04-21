package astava.samples.drawnmap;

import java.util.Arrays;

public class Tuple {
    private Object[] values;

    public Tuple(Object[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}

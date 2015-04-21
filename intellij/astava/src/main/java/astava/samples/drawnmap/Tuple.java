package astava.samples.drawnmap;

import java.util.Arrays;

public class Tuple {
    public final Object[] values;

    public Tuple(Object[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}

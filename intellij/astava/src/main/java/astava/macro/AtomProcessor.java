package astava.macro;

import astava.core.Atom;
import astava.core.Node;

import java.util.function.Function;

public class AtomProcessor<T, R> implements Processor {
    private Function<T, R> valueFunc;

    public AtomProcessor(Function<T, R> valueFunc) {
        this.valueFunc = valueFunc;
    }

    @Override
    public Node process(Node code) {
        R newValue = valueFunc.apply((T)((Atom)code).getValue());
        return new Atom(newValue);
    }
}

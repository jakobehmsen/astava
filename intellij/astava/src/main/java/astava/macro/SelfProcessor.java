package astava.macro;

import astava.core.Node;

import java.util.function.Consumer;
import java.util.function.Function;

public class SelfProcessor implements Processor {
    private Processor processor;

    public SelfProcessor(Function<Processor, Processor> processorFromSelf) {
        this.processor = processorFromSelf.apply(this);
    }

    @Override
    public Node process(Node code) {
        return processor.process(code);
    }
}

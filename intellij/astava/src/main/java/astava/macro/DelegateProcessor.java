package astava.macro;

import astava.core.Node;

public abstract class DelegateProcessor implements Processor {
    private Processor processor;

    public DelegateProcessor() {
        this.processor = createProcessor();
    }

    protected abstract Processor createProcessor();

    @Override
    public Node process(Node code) {
        return processor.process(code);
    }
}

package astava.macro;

import astava.core.Node;

public interface Processor {
    Node process(Node code);

    default Processor or(Processor other) {
        Processor self = this;
        return n -> {
            Node result = self.process(n);
            return result != null ? result : other.process(n);
        };
    }

    default Processor then(Processor next) {
        Processor self = this;
        return n -> {
            Node result = self.process(n);
            return result != null ? next.process(result) : null;
        };
    }

    default Processor forOperands(Processor processor) {
        return new OperandsProcessor(n -> this.process(n)).then(processor);
    }
}

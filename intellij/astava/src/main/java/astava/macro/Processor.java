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
}

package astava.macro;

import astava.core.Node;
import astava.core.Tuple;

import java.util.List;
import java.util.stream.Collectors;

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
}

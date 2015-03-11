package astava.macro;

import astava.core.Node;
import astava.core.Tuple;

import java.util.List;
import java.util.stream.Collectors;

public class RecursiveProcessor implements Processor {
    private Processor processor;

    public RecursiveProcessor(Processor processor) {
        this.processor = processor;
    }

    @Override
    public Node process(Node code) {
        Node result = processor.process(code);

        if(result == null) {
            if(code instanceof Tuple) {
                List<Node> elements = ((Tuple)code).stream().map(n ->
                    processOrKeep(n)).collect(Collectors.toList());
                return new Tuple(elements);
            }
        }

        return result;
    }

    private Node processOrKeep(Node code) {
        Node result = process(code);
        return result != null ? result : code;
    }
}

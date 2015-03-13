package astava.macro;

import astava.core.Node;
import astava.core.Tuple;

import java.util.List;
import java.util.stream.Collectors;

public class TupleProcessor implements Processor {
    private Processor elementProcessor;

    public TupleProcessor(Processor elementProcessor) {
        this.elementProcessor = elementProcessor;
    }

    @Override
    public Node process(Node code) {
        if(code instanceof Tuple) {
            List<Node> newElements = ((Tuple) code).stream().map(o ->
                elementProcessor.process(o)
            ).collect(Collectors.toList());
            return new Tuple(newElements);
        }

        return null;
    }
}

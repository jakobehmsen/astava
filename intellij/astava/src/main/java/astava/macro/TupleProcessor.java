package astava.macro;

import astava.core.Node;
import astava.core.Tuple;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TupleProcessor implements Processor {
    private Processor elementProcessor;
    private Function<List<Node>, Node> elementsProcessor;

    public TupleProcessor(Processor elementProcessor, Function<List<Node>, Node> elementsProcessor) {
        this.elementProcessor = elementProcessor;
        this.elementsProcessor = elementsProcessor;
    }

    @Override
    public Node process(Node code) {
        if(code instanceof Tuple) {
            List<Node> newElements = ((Tuple) code).stream().map(o ->
                elementProcessor.process(o)
            ).collect(Collectors.toList());
            return elementsProcessor.apply(newElements);
        }

        return null;
    }
}

package astava.macro;

import astava.core.Atom;
import astava.core.Node;
import astava.core.Tuple;

import java.util.List;
import java.util.stream.Collectors;

public class OperandsProcessor implements Processor {
    private Processor operandProcessor;

    public OperandsProcessor(Processor operandProcessor) {
        this.operandProcessor = operandProcessor;
    }

    @Override
    public Node process(Node code) {
        if(code instanceof Tuple) {
            Tuple tuple = (Tuple)code;
            if(tuple.size() > 0 && tuple.get(0) instanceof Atom) {
                List<Node> newElements = tuple.stream().skip(1).map(o ->
                    operandProcessor.process(o)
                ).collect(Collectors.toList());
                // Keep operator
                newElements.add(0, tuple.get(0));
                return new Tuple(newElements);
            }
        }

        return null;
    }
}

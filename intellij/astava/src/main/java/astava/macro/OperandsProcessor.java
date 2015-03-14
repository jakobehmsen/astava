package astava.macro;

import astava.core.Atom;
import astava.core.Node;
import astava.core.Tuple;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class OperandsProcessor implements Processor {
    private Processor operandProcessor;
    private BiFunction<Node, List<Node>, Node> operandsProcessor;

    public OperandsProcessor(Processor operandProcessor) {
        this(operandProcessor, true); // Keep operator by default
    }

    public OperandsProcessor(Processor operandProcessor, boolean keepOperator) {
        this(operandProcessor, (operator, newOperands) -> {
            if(keepOperator)
                newOperands.add(0, operator);
            return new Tuple(newOperands);
        });
    }

    public OperandsProcessor(Processor operandProcessor, BiFunction<Node, List<Node>, Node> operandsProcessor) {
        this.operandProcessor = operandProcessor;
        this.operandsProcessor = operandsProcessor;
    }

    @Override
    public Node process(Node code) {
        if(code instanceof Tuple) {
            Tuple tuple = (Tuple)code;
            if(tuple.size() > 0 && tuple.get(0) instanceof Atom) {
                List<Node> newOperands = tuple.stream().skip(1).map(o ->
                    operandProcessor.process(o)
                ).collect(Collectors.toList());

                Node operator = tuple.get(0);
                return operandsProcessor.apply(operator, newOperands);
            }
        }

        return null;
    }
}

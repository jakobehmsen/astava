package astava.macro;

import astava.core.Atom;
import astava.core.Node;
import astava.core.Tuple;
import astava.macro.Processor;

import java.util.function.Function;

public class OperatorProcessor<T> implements Processor {
    private T operator;
    private Processor processor;

    private Function<Node, T> operatorFunc;

    public OperatorProcessor(T operator, Processor processor, Function<Node, T> operatorFunc) {
        this.operator = operator;
        this.processor = processor;
        this.operatorFunc = operatorFunc;
    }

    @Override
    public Node process(Node code) {
        T operator = operatorFunc.apply(code);// getOperator(code);

        if(operator != null && processor.equals(operator)) {
            return processor.process(code);
        }

        return null;
    }

    /*
    private String getOperator(Node code) {
        if(code instanceof Tuple) {
            Tuple codeTuple = (Tuple)code;

            if(codeTuple.size() > 0 && codeTuple.get(0) instanceof Atom) {
                Atom firstElement = (Atom)codeTuple.get(0);
                if(firstElement.getValue() instanceof String)
                    return (String)firstElement.getValue();
            }
        }

        return null;
    }
    */
}

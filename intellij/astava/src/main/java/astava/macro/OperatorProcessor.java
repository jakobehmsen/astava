package astava.macro;

import astava.core.Atom;
import astava.core.Node;
import astava.core.Tuple;

public class OperatorProcessor implements Processor {
    private String operator;
    private Processor processor;

    public OperatorProcessor(String operator, Processor processor) {
        this.operator = operator;
        this.processor = processor;
    }

    @Override
    public Node process(Node code) {
        String operator = getOperator(code);

        if(operator != null && processor.equals(operator)) {
            return processor.process(code);
        }

        return null;
    }

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
}

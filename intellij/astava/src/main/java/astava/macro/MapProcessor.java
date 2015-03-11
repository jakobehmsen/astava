package astava.macro;

import astava.core.Atom;
import astava.core.Node;
import astava.core.Tuple;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MapProcessor<T> implements Processor {
    private Hashtable<T, Processor> map = new Hashtable<>();
    private Function<Node, T> operatorFunc;

    public MapProcessor(Function<Node, T> operatorFunc) {
        this.operatorFunc = operatorFunc;
    }

    public void put(T op, Processor processor) {
        map.put(op, processor);
    }

    @Override
    public Node process(Node code) {
        T operator = operatorFunc.apply(code);// getOperator(code);

        if(operator != null) {
            Processor processor = map.get(operator);
            if(processor != null) {
                return processCode(code, processor);
            }
        } else {
            if(code instanceof Tuple) {
                List<Node> elements = ((Tuple)code).stream().map(e -> process(e)).collect(Collectors.toList());
                return new Tuple(elements);
            }
        }

        //return code;
        return processOperands(code);
    }

    protected Node processCode(Node code, Processor processor) {
        code = processOperands(code);
        return processor.process(code);
    }

    public Node processOperands(Node code) {
        if(code instanceof Tuple) {
            Tuple tuple = (Tuple)code;
            List<Node> elements = IntStream.range(0, tuple.size())
                .mapToObj(i -> i > 0 ? process(tuple.get(i)) : tuple.get(i)).collect(Collectors.toList());

            return new Tuple(elements);
        }

        return code;
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

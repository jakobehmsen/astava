package astava.macro;

import astava.core.Atom;
import astava.core.Node;
import astava.core.Tuple;

import java.util.Hashtable;


public class MapProcessor implements Processor {
    private Hashtable<Object, Processor> map = new Hashtable<>();

    public MapProcessor put(Object op, Processor processor) {
        map.put(op, processor);
        return this;
    }

    @Override
    public Node process(Node code) {
        Object operator = getOperator(code);

        if(operator != null) {
            Processor processor = map.get(operator);
            if(processor != null)
                return processor.process(code);
        }
        return null;
    }

    private Object getOperator(Node code) {
        if(code instanceof Tuple) {
            Tuple codeTuple = (Tuple)code;

            if(codeTuple.size() > 0 && codeTuple.get(0) instanceof Atom) {
                Atom firstElement = (Atom)codeTuple.get(0);
                return firstElement.getValue();
            }
        }

        return null;
    }
}

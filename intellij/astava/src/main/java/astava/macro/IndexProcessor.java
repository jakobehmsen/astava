package astava.macro;

import astava.core.Node;
import astava.core.Tuple;

import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IndexProcessor implements Processor {
    private Hashtable<Integer, Processor> indexToProcessorMap = new Hashtable<>();

    public IndexProcessor set(int index, Processor processor) {
        indexToProcessorMap.put(index, processor);
        return this;
    }

    @Override
    public Node process(Node code) {
        Tuple tuple = (Tuple)code;
        List<Node> elements = IntStream.range(0, tuple.size())
            .mapToObj(i -> processIndex(tuple, i)).collect(Collectors.toList());

        return new Tuple(elements);
    }

    private Node processIndex(Tuple tuple, int index) {
        Processor processor = indexToProcessorMap.get(index);
        if(processor != null)
            return processor.process(tuple.get(index));
        return tuple.get(index);
    }
}

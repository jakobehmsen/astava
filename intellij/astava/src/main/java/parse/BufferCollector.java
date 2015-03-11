package parse;

import astava.core.Node;

import java.util.List;

public class BufferCollector implements Collector {
    private List<Node> buffer;

    public BufferCollector(List<Node> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void put(Node node) {
        buffer.add(node);
    }

    @Override
    public void putAll(List<Node> nodes) {
        buffer.addAll(nodes);
    }
}

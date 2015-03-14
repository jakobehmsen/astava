package parse;

import astava.core.Node;

import java.util.List;

public interface Collector {
    void put(Node node);
    default void putAll(List<Node> nodes) {
        nodes.forEach(n -> put(n));
    }
}

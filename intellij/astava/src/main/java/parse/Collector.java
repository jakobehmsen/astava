package parse;

import astava.core.Node;

import java.util.List;

public interface Collector {
    void put(Node node);
    void putAll(List<Node> nodes);
}

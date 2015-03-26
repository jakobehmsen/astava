package astava.parse3.tree;

import astava.core.Node;
import astava.core.Tuple;
import astava.parse3.*;

import java.util.stream.Collectors;

public class NodeParse {
    public static Parser<Tuple, Node> descent(Parser<Node, Node> elementParser) {
        return Parse.<Tuple, Node, Node, Node>descent(
            tuple -> new ListInput(tuple),
            nodes -> new Tuple(nodes.stream().collect(Collectors.toList())),
            elementParser
        );
    }
}

package astava.parse.tree;

import astava.tree.Node;
import astava.tree.Tuple;
import astava.parse.*;

import java.util.function.Function;
import java.util.stream.Collectors;

public class NodeParse {
    public static Parser<Tuple, Node> descentReduce(Parser<Node, Node> elementParser, Function<Input<Node>, Node> reducer) {
        return Parse.<Tuple, Node, Node, Node>descentReduce(
            tuple -> new ListInput(tuple),
            reducer,
            elementParser
        );
    }

    public static Parser<Tuple, Node> descentReduce(Parser<Node, Node> elementParser) {
        return descentReduce(
            elementParser,
            nodes -> new Tuple(nodes.stream().collect(Collectors.toList())));
    }
}

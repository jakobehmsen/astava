package astava.parse3.tree;

import astava.core.Node;
import astava.core.Tuple;
import astava.parse3.Cursor;
import astava.parse3.Matcher;
import astava.parse3.Parser;

public class NodeParse {
    public static <TIn> Parser<Tuple, Tuple> descent(Parser<TIn, Tuple> parser, Parser<Node, Node> elementParser) {
        return new Parser<Tuple, Tuple>() {
            @Override
            public void parse(Cursor<Tuple> cursor, Matcher<Tuple, Tuple> matcher) {

            }
        };
    }
}

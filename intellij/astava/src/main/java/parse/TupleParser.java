package parse;

import astava.core.Node;
import astava.core.Tuple;

import java.util.ArrayList;
import java.util.Map;

public class TupleParser implements Parser {
    private Parser elementsParser;

    public TupleParser(Parser elementsParser) {
        this.elementsParser = elementsParser;
    }


    @Override
    public void parse(Matcher matcher, Map<String, Parser> rules) {
        if(matcher.peekByte() == '(') {
            matcher.consume();

            matcher.ignoreWS();

            ArrayList<Node> elements = new ArrayList<>();
            Matcher elementsMatcher = matcher.beginMatch(new BufferCollector(elements));
            elementsParser.parse(elementsMatcher, rules);
            if(elementsMatcher.matched()) {
                if(matcher.peekByte() == ')') {
                    matcher.consume();

                    matcher.put(new Tuple(elements));
                    matcher.match();
                }
            }
        }
    }
}

package parse;

import java.util.Map;

public class ElementsParser implements Parser {
    private Parser elementParser;

    public ElementsParser(Parser elementParser) {
        this.elementParser = elementParser;
    }

    @Override
    public void parse(Matcher matcher, Map<String, Parser> rules) {
        matcher.ignoreWS();
        Matcher elementMatcher = matcher.beginMatch();
        elementParser.parse(elementMatcher, rules);
        while(elementMatcher.matched()) {
            matcher.ignoreWS();
            elementMatcher = matcher.beginMatch();
            elementParser.parse(elementMatcher, rules);
        }
        matcher.ignoreWS();
        matcher.match();
    }
}

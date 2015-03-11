package parse;

import java.util.Map;

public class RuleParser implements Parser {
    private String name;

    public RuleParser(String name) {
        this.name = name;
    }

    @Override
    public void parse(Matcher matcher, Map<String, Parser> rules) {
        Parser parser = rules.get(name);
        parser.parse(matcher, rules);
    }
}

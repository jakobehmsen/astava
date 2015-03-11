package parse;

import java.util.Map;

public interface Parser {
    void parse(Matcher matcher, Map<String, Parser> rules);

    default Parser or(Parser other) {
        Parser self = this;

        return (matcher, rules) -> {
            Matcher thisMatcher = matcher.beginMatch();
            self.parse(thisMatcher, rules);
            if(thisMatcher.matched()) {
                matcher.match();
            } else {
                other.parse(matcher, rules);
            }
        };
    }

    default Parser trim(Parser trimmer) {
        Parser self = this;

        return (matcher, rules) -> {
            trimmer.parse(matcher, rules);
            self.parse(matcher, rules);
            trimmer.parse(matcher, rules);
        };
    }

    default Parser then(Parser next) {
        Parser self = this;

        return (matcher, rules) -> {
            Matcher firstMatcher = matcher.beginMatch();
            self.parse(firstMatcher, rules);
            if(firstMatcher.matched())
                next.parse(matcher, rules);
        };
    }
}

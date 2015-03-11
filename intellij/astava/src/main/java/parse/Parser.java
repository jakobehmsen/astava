package parse;

public interface Parser {
    void parse(Matcher matcher);

    default Parser or(Parser other) {
        Parser self = this;

        return matcher -> {
            Matcher thisMatcher = matcher.beginMatch();
            self.parse(thisMatcher);
            if(thisMatcher.matched()) {
                matcher.match();
            } else {
                other.parse(matcher);
            }
        };
    }

    default Parser then(Parser next) {
        Parser self = this;

        return matcher -> {
            Matcher firstMatcher = matcher.beginMatch();
            self.parse(firstMatcher);
            if(firstMatcher.matched())
                next.parse(matcher);
        };
    }
}

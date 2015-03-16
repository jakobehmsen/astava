package astava.parse;

public interface Parser {
    void parse(Matcher matcher);

    default Parser or(Parser other) {
        Parser self = this;

        return matcher -> {
            Matcher thisMatcher = matcher.beginMatch();
            self.parse(thisMatcher);
            if(thisMatcher.accepted()) {
                matcher.accept();
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
            if(firstMatcher.accepted())
                next.parse(matcher);
            else
                matcher.reject();
        };
    }
}

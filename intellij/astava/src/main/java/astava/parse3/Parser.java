package astava.parse3;

import java.util.function.BiFunction;

public interface Parser<T> {
    <R extends Matcher<T>> R parse(Input<T> input, R matcher);
    default <R extends Matcher<T>> R parseInit(Input<T> input, BiFunction<Parser<T>, Input<T>, R> matcherFunction) {
        R matcher = matcherFunction.apply(this, input);
        parse(input, matcher);
        return matcher;
    }

    default Parser<T> or(Parser<T> other) {
        return Parse.decision(this, other);
    }

    default Parser<T> then(Parser<T> next) {
        return Parse.sequence(this, next);
    }
}

package astava.parse3;

import java.util.List;
import java.util.function.BiFunction;

public interface Parser<TIn, TOut> {
    void parse(Input<TIn> input, Matcher<TIn, TOut> matcher);
    default Matcher<TIn, TOut> parseInit(Input<TIn> input, BiFunction<Parser<TIn, TOut>, Input<TIn>, Matcher<TIn, TOut>> matcherFunction) {
        Matcher<TIn, TOut> matcher = matcherFunction.apply(this, input);
        parse(input, matcher);
        return matcher;
    }

    default Parser<TIn, TOut> or(Parser<TIn, TOut> other) {
        return Parse.decision(this, other);
    }

    default Parser<TIn, TOut> then(Parser<TIn, TOut> next) {
        return Parse.sequence(this, next);
    }

    default <TOut2> Parser<TIn, TOut2> pipe(Parser<TOut, TOut2> next) {
        return Parse.pipe(this, next);
    }

    default <TOut2> Parser<TIn, TOut2> pipeOut(Parser<Input<TOut>, TOut2> next) {
        return Parse.pipeOut(this, next);
    }

    default Parser<TIn, TOut> multi() {
        return Parse.multi(this);
    }

    default Parser<TIn, TOut> onceOrMore() {
        return Parse.onceOrMore(this);
    }
}

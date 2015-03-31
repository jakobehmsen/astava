package astava.parse;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ParserWrapper<TIn, TInter, TOut> implements Parser<TIn, TOut> {
    private Parser<TIn, TInter> parser;
    private BiFunction<Cursor<TIn>, Matcher<TIn, TOut>, Consumer<Input<TInter>>> wrapper;

    public ParserWrapper(Parser<TIn, TInter> parser, BiFunction<Cursor<TIn>, Matcher<TIn, TOut>, Consumer<Input<TInter>>> wrapper) {
        this.parser = parser;
        this.wrapper = wrapper;
    }

    @Override
    public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
        Matcher<TIn, TInter> wrappedMatcher = matcher.beginVisit(parser, cursor);
        Consumer<Input<TInter>> producer = wrapper.apply(cursor, matcher);

        parser.parse(cursor, wrappedMatcher);

        if(wrappedMatcher.isMatch()) {
            producer.accept(wrappedMatcher.production());
            matcher.visitSuccess();
        } else
            matcher.visitFailure();
    }

    @Override
    public String toString() {
        return parser.toString() + " :> " + wrapper;
    }
}

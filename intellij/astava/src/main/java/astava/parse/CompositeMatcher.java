package astava.parse;

import java.util.List;
import java.util.stream.Collectors;

public class CompositeMatcher<TIn, TOut> implements Matcher<TIn, TOut> {
    public interface Reducer<TIn, TOut> {
        Input<TOut> production(List<Matcher<TIn, TOut>> matchers);
        boolean isMatch(List<Matcher<TIn, TOut>> matchers);
    }

    private List<Matcher<TIn, TOut>> matchers;
    private Reducer<TIn, TOut> reducer;

    public CompositeMatcher(List<Matcher<TIn, TOut>> matchers) {
        this(matchers, new Reducer<TIn, TOut>() {
            @Override
            public Input<TOut> production(List<Matcher<TIn, TOut>> matchers) {
                return matchers.get(0).production();
            }

            @Override
            public boolean isMatch(List<Matcher<TIn, TOut>> matchers) {
                return matchers.get(0).isMatch();
            }
        });
    }

    public CompositeMatcher(List<Matcher<TIn, TOut>> matchers, Reducer<TIn, TOut> reducer) {
        this.matchers = matchers;
        this.reducer = reducer;
    }

    @Override
    public void visitSuccess() {
        matchers.forEach(m -> m.visitSuccess());
    }

    @Override
    public void visitFailure() {
        matchers.forEach(m -> m.visitFailure());
    }

    @Override
    public void put(TOut value) {
        matchers.forEach(m -> m.put(value));
    }

    @Override
    public Input<TOut> production() {
        return reducer.production(matchers);
    }

    @Override
    public <TIn, TOut> Matcher<TIn, TOut> beginVisit(Parser<TIn, TOut> parser, Cursor<TIn> cursor) {
        return new CompositeMatcher<>(matchers.stream().map(m ->
            m.beginVisit(parser, cursor)).collect(Collectors.toList()));
    }

    @Override
    public boolean isMatch() {
        return reducer.isMatch(matchers);
    }
}

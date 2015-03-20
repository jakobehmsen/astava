package astava.parse3;

public interface Matcher<TIn, TOut> {
    void visitSuccess();
    void visitFailure();
    void put(TOut value);
    Input<TOut> production();

    <TIn, TOut> Matcher<TIn, TOut> beginVisit(Parser<TIn, TOut> parser, Input<TIn> input);
    boolean isMatch();

    default void propagate(Matcher<TIn, TOut> matcher) {
        if(isMatch()) {
            matcher.visitSuccess();
            propagateOutput(matcher);
        } else
            matcher.visitFailure();
    }

    default <TIn> void propagateOutput(Matcher<TIn, TOut> matcher) {
        Input<TOut> production = production();
        while(!production.atEnd()) {
            matcher.put(production.peek());
            production.consume();
        }
    }
}

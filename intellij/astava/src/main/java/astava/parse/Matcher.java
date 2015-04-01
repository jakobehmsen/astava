package astava.parse;

public interface Matcher<TIn, TOut> {
    void visitSuccess();
    void visitFailure();
    void put(TOut value);
    Input<TOut> production();

    <TIn, TOut> Matcher<TIn, TOut> beginVisit(Parser<TIn, TOut> parser, Cursor<TIn> cursor);
    boolean isMatch();

    default void visitSuccess(TOut value) {
        put(value);
        visitSuccess();
    }

    default void propagate(Matcher<TIn, TOut> matcher) {
        if(isMatch()) {
            matcher.visitSuccess();
            propagateOutput(matcher);
        } else
            matcher.visitFailure();
    }

    default <TIn, TOut> void propagateIsMatch(Matcher<TIn, TOut> matcher) {
        if(isMatch()) {
            matcher.visitSuccess();
        } else
            matcher.visitFailure();
    }

    default <TIn> void propagateOutput(Matcher<TIn, TOut> matcher) {
        matcher.put(production());
        /*Cursor<TOut> production = production().cursor();
        while(!production.atEnd()) {
            matcher.put(production.peek());
            production.consume();
        }*/
    }

    default void put(Input<TOut> input) {
        Cursor<TOut> productionCursor = input.cursor();
        while(!productionCursor.atEnd()) {
            put(productionCursor.peek());
            productionCursor.consume();
        }
    }
}

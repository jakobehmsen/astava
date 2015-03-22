package astava.parse3;

public class AbstractMatcher<TIn, TOut> implements Matcher<TIn, TOut> {
    @Override
    public void visitSuccess() {

    }

    @Override
    public void visitFailure() {

    }

    @Override
    public void put(TOut value) {

    }

    @Override
    public Input<TOut> production() {
        return null;
    }

    @Override
    public <TIn, TOut> Matcher<TIn, TOut> beginVisit(Parser<TIn, TOut> parser, Cursor<TIn> cursor) {
        return NullMatcher.INSTANCE;
    }

    @Override
    public boolean isMatch() {
        return false;
    }
}

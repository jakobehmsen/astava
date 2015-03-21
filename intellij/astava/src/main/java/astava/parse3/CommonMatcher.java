package astava.parse3;

import java.util.ArrayList;

public class CommonMatcher<TIn, TOut> implements Matcher<TIn, TOut> {
    private Boolean result;

    @Override
    public void visitSuccess() {
        result = true;
    }

    @Override
    public void visitFailure() {
        result = false;
    }

    private ArrayList<TOut> production = new ArrayList<>();

    @Override
    public void put(TOut value) {
        production.add(value);
    }

    @Override
    public Input<TOut> production() {
        return new ListInput<>(production);
    }

    @Override
    public <TIn, TOut> Matcher<TIn, TOut> beginVisit(Parser<TIn, TOut> parser, Cursor<TIn> cursor) {
        return new CommonMatcher();
    }

    @Override
    public boolean isMatch() {
        return result;
    }
}

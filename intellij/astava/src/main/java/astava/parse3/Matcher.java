package astava.parse3;

public interface Matcher<T> {
    void visitSuccess();
    void visitFailure();

    Matcher<T> beginVisit(Parser<T> parser, Input<T> input);
    boolean isMatch();

    default void propagate(Matcher<T> matcher) {
        if(isMatch())
            matcher.visitSuccess();
        else
            matcher.visitFailure();
    }
}

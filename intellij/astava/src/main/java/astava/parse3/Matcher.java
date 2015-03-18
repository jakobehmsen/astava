package astava.parse3;

public interface Matcher<T> {

    void visitPreInput(Input<T> input);
    void visitCaptured(T captured);
    void visitPostInput(Input<T> input);
    void visitSuccess();
    void visitFailure();

    Matcher<T> beginVisit(Parser<T> parser);
    boolean endVisit();
}

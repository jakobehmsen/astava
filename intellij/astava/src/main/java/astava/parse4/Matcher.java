package astava.parse4;

public interface Matcher<T> {
    boolean canDescend();
    Matcher descend();
    Matcher ascend();
    boolean hasNext();
    void moveNext();
    boolean isMatch();
    int start();
    int end();
    Input<T> input();
    default Input<T> captured() {
        return input().interval(start(), end());
    }
}

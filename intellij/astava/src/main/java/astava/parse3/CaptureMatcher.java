package astava.parse3;

public interface CaptureMatcher<T> extends Matcher<T> {
    Input<T> captured();
}

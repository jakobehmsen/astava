package astava.parse4;

public interface Parser<T> {
    Matcher<T> parse(Input<T> input);
}

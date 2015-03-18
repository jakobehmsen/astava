package astava.parse3;

public interface Parser<T> {
    void parse(Input<T> input, Matcher matcher);
}

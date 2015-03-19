package astava.parse4;

import java.util.List;

public class SequenceParser<T> implements Parser<T> {
    private List<Parser<T>> parsers;

    public SequenceParser(List<Parser<T>> parsers) {
        this.parsers = parsers;
    }

    @Override
    public Matcher<T> parse(Input<T> input) {
        return new Matcher<T>() {
            @Override
            public boolean canDescend() {
                return false;
            }

            @Override
            public Matcher descend() {
                return null;
            }

            @Override
            public Matcher ascend() {
                return null;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public void moveNext() {

            }

            @Override
            public boolean isMatch() {
                return false;
            }

            @Override
            public int start() {
                return 0;
            }

            @Override
            public int end() {
                return 0;
            }

            @Override
            public Input<T> input() {
                return input;
            }
        };
    }
}

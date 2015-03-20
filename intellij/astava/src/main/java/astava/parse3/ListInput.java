package astava.parse3;

import java.util.List;

public class ListInput<TIn> implements Input<TIn> {
    private class IndexHolder implements Position<TIn> {
        public final int value;

        private IndexHolder(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "At " + value + " of " + ListInput.this.list;
        }
    }

    private List<TIn> list;
    private int index;

    public ListInput(List<TIn> list) {
        this.list = list;
    }

    @Override
    public Position<TIn> position() {
        return new IndexHolder(index);
    }

    @Override
    public Input<TIn> interval(Position<TIn> start, Position<TIn> end) {
        /*return new CharSequenceInput(chars.subSequence(
                ((IndexHolder)start).value, ((IndexHolder)end).value
        ));*/
        return null;
    }

    @Override
    public TIn peek() {
        return list.get(index);
    }

    @Override
    public void consume() {
        index++;
    }

    @Override
    public boolean atEnd() {
        return index >= list.size();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public void setPosition(Position<TIn> position) {
        index = ((IndexHolder)position).value;
    }
}

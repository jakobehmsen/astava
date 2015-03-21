package astava.parse3;

import java.util.List;

public class ListInput<TIn> implements Cursor<TIn> {
    /*private class IndexHolder implements Position<TIn> {
        public final int value;

        private IndexHolder(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "At " + value + " of " + ListInput.this.list;
        }
    }*/

    private List<TIn> list;
    private int index;

    public ListInput(List<TIn> list) {
        this.list = list;
    }

    @Override
    public State state() {
        int index = this.index;
        return new State() {
            @Override
            public void restore() {
                ListInput.this.index = index;
            }

            @Override
            public String toString() {
                return "" + index;
            }
        };
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
}

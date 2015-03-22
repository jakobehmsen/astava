package astava.parse3;

import java.util.List;

public class ListCursor<TIn> implements Cursor<TIn> {
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

    public ListCursor(List<TIn> list) {
        this.list = list;
    }

    private class ListState implements CursorState {
        private int index;

        private ListState(int index) {
            this.index = index;
        }

        @Override
        public void restore() {
            ListCursor.this.index = index;
        }

        @Override
        public int compareTo(CursorState o) {
            return index - ((ListState)o).index;
        }

        @Override
        public String toString() {
            return "" + index;
        }
    }

    @Override
    public CursorState state() {
        return new ListState(index);
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

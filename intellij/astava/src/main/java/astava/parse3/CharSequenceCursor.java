package astava.parse3;

public class CharSequenceCursor implements Cursor<Character> {
    /*private class IndexHolder implements Position<Character> {
        public final int value;

        private IndexHolder(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "" + value;
        }
    }*/

    private CharSequence chars;
    private int index;
    private CursorStateFactory<Character, CharSequenceCursor> stateStrategy;

    public CharSequenceCursor(CharSequence chars) {
        this(chars, new CharSequenceStateStrategy());
    }

    public void setStateStrategy(CursorStateFactory<Character, CharSequenceCursor> stateStrategy) {
        this.stateStrategy = stateStrategy;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public CharSequenceCursor(CharSequence chars, CursorStateFactory<Character, CharSequenceCursor> stateStrategy) {
        this.chars = chars;
        this.stateStrategy = stateStrategy;
    }

    private static class CharSequenceStateStrategy implements CursorStateFactory<Character, CharSequenceCursor> {
        @Override
        public void consume(java.lang.Character value) {
            consume(value.charValue());
        }

        @Override
        public CursorState getState(CharSequenceCursor cursor) {
            return new CharSequenceState(cursor, cursor.index);
        }

        @Override
        public void consume(char ch) { }
    }

    private static class CharSequenceState implements CursorState {
        private CharSequenceCursor cursor;
        private int index;

        private CharSequenceState(CharSequenceCursor cursor, int index) {
            this.cursor = cursor;
            this.index = index;
        }

        @Override
        public void restore() {
            cursor.index = index;
        }

        @Override
        public int compareTo(CursorState o) {
            return index - ((CharSequenceState)o).index;
        }

        @Override
        public String toString() {
            return "" + index;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CharSequenceState && index == ((CharSequenceState)obj).index;
        }
    }

    @Override
    public CursorState state() {
        //return new CharSequenceState(index);
        return stateStrategy.getState(this);
    }

    /*@Override
    public Position<Character> position() {
        return new IndexHolder(index);
    }

    @Override
    public Cursor<Character> interval(Position<Character> start, Position<Character> end) {
        return new CharSequenceInput(chars.subSequence(
            ((IndexHolder)start).value, ((IndexHolder)end).value
        ));
    }*/

    @Override
    public Character peek() {
        return chars.charAt(index);
    }

    @Override
    public void consume() {
        index++;
        stateStrategy.consume(chars.charAt(index - 1));
    }

    @Override
    public boolean atEnd() {
        return index >= chars.length();
    }

    @Override
    public String toString() {
        return chars.toString();
    }

    /*@Override
    public void setPosition(Position<Character> position) {
        index = ((IndexHolder)position).value;
    }*/
}

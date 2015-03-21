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

    public CharSequenceCursor(CharSequence chars) {
        this.chars = chars;
    }

    private class CharSequenceState implements State {
        private int index;

        private CharSequenceState(int index) {
            this.index = index;
        }

        @Override
        public void restore() {
            CharSequenceCursor.this.index = index;
        }

        @Override
        public int compareTo(State o) {
            return index - ((CharSequenceState)o).index;
        }

        @Override
        public String toString() {
            return "" + index;
        }
    }

    @Override
    public State state() {
        return new CharSequenceState(index);
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

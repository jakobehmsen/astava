package astava.parse3;

public class CharSequenceInput implements Cursor<Character> {
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

    public CharSequenceInput(CharSequence chars) {
        this.chars = chars;
    }

    @Override
    public State state() {
        int index = this.index;
        return new State() {
            @Override
            public void restore() {
                CharSequenceInput.this.index = index;
            }

            @Override
            public String toString() {
                return "" + index;
            }
        };
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

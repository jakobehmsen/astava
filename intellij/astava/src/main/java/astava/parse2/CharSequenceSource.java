package astava.parse2;

public class CharSequenceSource implements Source<Character> {
    private CharSequence chars;
    private int index;

    public CharSequenceSource(CharSequence chars) {
        this(chars, 0);
    }

    public CharSequenceSource(CharSequence chars, int index) {
        this.chars = chars;
        this.index = index;
    }

    @Override
    public Character get() {
        return getChar();
    }

    @Override
    public char getChar() {
        return chars.charAt(index);
    }

    @Override
    public Source<Character> rest() {
        return new CharSequenceSource(chars, index + 1);
    }

    @Override
    public boolean atEnd() {
        return index >= chars.length();
    }

    @Override
    public String toString() {
        return "At " + index;
    }
}

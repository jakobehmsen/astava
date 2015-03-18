package astava.parse3;

public class CharSequenceInput implements Input<Character> {
    private CharSequence chars;
    private int index;

    public CharSequenceInput(CharSequence chars) {
        this.chars = chars;
    }

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
        return "At " + index + " of " + chars;
    }
}

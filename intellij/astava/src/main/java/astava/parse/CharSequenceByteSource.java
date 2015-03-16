package astava.parse;

public class CharSequenceByteSource implements ByteSource {
    private CharSequence charSequence;

    public CharSequenceByteSource(CharSequence charSequence) {
        this.charSequence = charSequence;
    }

    @Override
    public int get(int index) {
        return index < charSequence.length() ? charSequence.charAt(index) : -1;
    }
}

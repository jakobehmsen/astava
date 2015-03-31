package astava.parse;

public interface CursorState extends Comparable<CursorState> {
    void restore();
}

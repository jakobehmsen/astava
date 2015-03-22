package astava.parse3;

public interface CursorState extends Comparable<CursorState> {
    void restore();
}

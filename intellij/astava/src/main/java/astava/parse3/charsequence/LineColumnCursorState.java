package astava.parse3.charsequence;

import astava.parse3.CursorState;

public class LineColumnCursorState implements CursorState {
    private CharSequenceCursor cursor;
    private int index;
    private int line;
    private int column;
    private boolean lastWasCR;

    public LineColumnCursorState(CharSequenceCursor cursor, int line, int column, boolean lastWasCR) {
        this.cursor = cursor;
        this.index = cursor.getIndex();
        this.line = line;
        this.column = column;
        this.lastWasCR = lastWasCR;
    }

    @Override
    public void restore() {
        cursor.setIndex(index);
        cursor.setStateStrategy(new LineColumnCursorStateFactory(line, column, lastWasCR));
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LineColumnCursorState && index == ((LineColumnCursorState)obj).index;
    }

    @Override
    public int compareTo(CursorState o) {
        return index - ((LineColumnCursorState)o).index;
    }

    @Override
    public String toString() {
        return "(" + line + "," + column + ")";
    }
}

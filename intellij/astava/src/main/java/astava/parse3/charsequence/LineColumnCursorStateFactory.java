package astava.parse3.charsequence;

import astava.parse3.CursorState;
import astava.parse3.CursorStateFactory;

public class LineColumnCursorStateFactory implements CursorStateFactory<Character, CharSequenceCursor> {
    private int line;
    private int column;
    private boolean lastWasCR;

    public LineColumnCursorStateFactory() { }

    public LineColumnCursorStateFactory(int line, int column, boolean lastWasCR) {
        this.line = line;
        this.column = column;
        this.lastWasCR = lastWasCR;
    }

    @Override
    public void consume(Character value) {
        consume(value.charValue());
    }

    @Override
    public CursorState getState(CharSequenceCursor cursor) {
        return new LineColumnCursorState(cursor, line, column, lastWasCR);
    }

    @Override
    public void consume(char ch) {
        if(lastWasCR) {
            switch (ch) {
                case '\r':
                    lastWasCR = true;
                    break;
                case '\n':
                    line++;
                    column = 0;
                    lastWasCR = false;
                    break;
                default:
                    column++;
                    lastWasCR = false;
                    break;
            }
        } else {
            switch (ch) {
                case '\n':
                    line++;
                    column = 0;
                    break;
                case '\r':
                    lastWasCR = true;
                    break;
                default:
                    column++;
                    break;
            }
        }
    }
}

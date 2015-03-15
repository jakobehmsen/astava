package parse;

import astava.core.Atom;
import astava.core.Node;

import java.util.List;

public interface Matcher extends Collector {
    public static final int UNDETERMINED = 0;
    public static final int ACCEPTED = 1;
    public static final int REJECTED = 2;

    default Matcher beginMatch() {
        return beginMatch(this);
    }
    Matcher beginMatch(Collector collector);
    int state();
    void accept();
    void reject();
    void error(String message);

    int peekByte();
    void consume();

    default void ignoreWS() {
        while(peekByte() == ' ' || peekByte() == '\r' || peekByte() == '\n')
            consume();
    }

    default boolean accepted() {
        return state() == ACCEPTED;
    }
}

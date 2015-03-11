package parse;

import astava.core.Atom;
import astava.core.Node;

import java.util.List;

public interface Matcher extends Collector {
    default Matcher beginMatch() {
        return beginMatch(this);
    }
    Matcher beginMatch(Collector collector);
    boolean matched();
    void match();
    int peekByte();
    void consume();

    default void ignoreWS() {
        while(peekByte() == ' ' || peekByte() == '\r' || peekByte() == '\n')
            consume();
    }
}

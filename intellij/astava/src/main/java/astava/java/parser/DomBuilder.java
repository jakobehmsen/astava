package astava.java.parser;

import java.io.Serializable;

public interface DomBuilder extends Serializable {
    void accept(DomBuilderVisitor visitor);
}

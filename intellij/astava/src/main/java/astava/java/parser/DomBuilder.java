package astava.java.parser;

import astava.tree.CodeDom;
import astava.tree.StatementDom;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface DomBuilder extends Serializable {
    void accept(DomBuilderVisitor visitor);

    default boolean test(CodeDom code, Map<String, Object> captures) {
        return false;
    }

    default CodeDom map(List<Object> captures) {
        throw new UnsupportedOperationException("Cannot do map (yet?).");
    }
}

package astava.java.parser;

import astava.tree.StatementDom;

import java.util.Map;
import java.util.Set;

public interface StatementDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitStatementBuilder(this);
    }

    default void appendLocals(Map<String, String> locals) { }
    StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext);
}

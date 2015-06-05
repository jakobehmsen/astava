package astava.java.parser;

import astava.tree.StatementDom;

import java.util.Set;

public interface StatementDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitStatementBuilder(this);
    }

    default void appendLocals(Set<String> locals) { }
    StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Set<String> locals);
}

package astava.java.parser;

import astava.tree.ExpressionDom;

import java.util.Set;

public interface ExpressionDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitExpressionBuilder(this);
    }
    ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, Set<String> locals);
}

package astava.java.parser;

import astava.tree.ExpressionDom;

public interface ExpressionDomBuilder {
    ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration);
}

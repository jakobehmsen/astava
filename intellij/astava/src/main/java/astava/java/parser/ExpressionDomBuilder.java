package astava.java.parser;

import astava.tree.ExpressionDom;

import java.util.Set;

public interface ExpressionDomBuilder {
    ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, Set<String> locals);
}

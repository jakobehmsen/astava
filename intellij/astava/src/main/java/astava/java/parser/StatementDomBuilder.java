package astava.java.parser;

import astava.tree.StatementDom;

public interface StatementDomBuilder {
    StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration);
}

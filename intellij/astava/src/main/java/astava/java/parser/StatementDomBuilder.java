package astava.java.parser;

import astava.tree.StatementDom;

import java.util.Set;

public interface StatementDomBuilder {
    default void appendLocals(Set<String> locals) { }
    StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, Set<String> locals);
}

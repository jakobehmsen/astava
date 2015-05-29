package astava.java.parser;

import astava.tree.MethodDom;

public interface MethodDomBuilder {
    MethodDom build(ClassResolver classResolver, ClassDeclaration classDeclaration);
}

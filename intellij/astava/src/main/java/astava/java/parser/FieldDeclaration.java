package astava.java.parser;

import astava.tree.FieldDom;

public interface FieldDeclaration {
    int getModifiers();
    String getTypeName();
    String getName();
    FieldDom build(ClassDeclaration classDeclaration);
}

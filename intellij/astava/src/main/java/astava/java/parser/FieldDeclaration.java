package astava.java.parser;

import astava.tree.FieldDom;

public interface FieldDeclaration {
    int getModifier();
    String getTypeName();
    String getName();
    FieldDom build(ClassDeclaration classDeclaration);
}

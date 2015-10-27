package astava.java.parser;

import astava.tree.CodeDom;

import java.util.Map;

public interface CodeDomBuilder {
    CodeDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals);
}

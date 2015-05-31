package astava.java.parser;

import astava.tree.MethodDom;
import astava.tree.ParameterInfo;

import java.util.List;

public interface MethodDeclaration {
    int getModifiers();
    String getName();
    List<ParameterInfo> getParameterTypes();
    String getReturnTypeName();
    MethodDom build(ClassDeclaration classDeclaration);
}

package astava.java.parser;

import java.util.List;

public interface ClassDeclaration {
    List<FieldDeclaration> getFields();
    List<MethodDeclaration> getMethods();
    String getName();
}

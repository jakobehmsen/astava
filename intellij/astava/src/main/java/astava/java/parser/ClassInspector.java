package astava.java.parser;

public interface ClassInspector {
    ClassDeclaration getClassDeclaration(String name);
    default ClassDeclaration getClassDeclarationFromDescriptor(String name) {
        return getClassDeclaration(name.replace('/', '.'));
    }
}

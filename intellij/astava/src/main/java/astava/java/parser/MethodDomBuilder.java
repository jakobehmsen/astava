package astava.java.parser;

public interface MethodDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitMethodBuilder(this);
    }

    MethodDeclaration declare(ClassResolver classResolver);

    String getName();
}

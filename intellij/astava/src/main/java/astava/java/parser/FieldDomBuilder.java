package astava.java.parser;

public interface FieldDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitFieldBuilder(this);
    }

    FieldDeclaration declare(ClassResolver classResolver);
}

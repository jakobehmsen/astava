package astava.java.parser;

import astava.tree.ClassDom;

public interface ClassDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitClassBuilder(this);
    }

    ClassDeclaration build(ClassResolver classResolver);

    String getName();
}

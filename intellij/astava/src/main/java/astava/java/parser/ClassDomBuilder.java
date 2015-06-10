package astava.java.parser;

import java.util.List;

public interface ClassDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitClassBuilder(this);
    }

    ClassDeclaration build(ClassResolver classResolver);

    String getName();

    List<FieldDomBuilder> getFields();

    List<MethodDomBuilder> getMethods();

    String getSuperName();
}

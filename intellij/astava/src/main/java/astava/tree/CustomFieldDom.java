package astava.tree;

public interface CustomFieldDom extends FieldDom {
    default <T> T accept(FieldDomVisitor<T> visitor) {
        return visitor.visitCustomField(this);
    }

    int getModifiers();

    String getName();

    String getTypeName();
}

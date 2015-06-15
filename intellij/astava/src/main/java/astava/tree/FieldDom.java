package astava.tree;

public interface FieldDom {
    String getName();
    <T> T accept(FieldDomVisitor<T> visitor);
}

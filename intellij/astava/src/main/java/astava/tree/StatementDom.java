package astava.tree;

public interface StatementDom extends CodeDom {
    default void accept(CodeDomVisitor visitor) {
        visitor.visitStatement(this);
    }
    void accept(StatementDomVisitor visitor);
}

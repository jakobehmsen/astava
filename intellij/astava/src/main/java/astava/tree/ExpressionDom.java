package astava.tree;

public interface ExpressionDom extends CodeDom {
    default void accept(CodeDomVisitor visitor) {
        visitor.visitExpression(this);
    }
    void accept(ExpressionDomVisitor visitor);
    default boolean equals(ExpressionDom other, CodeDomComparison context) {
        return false;
    }
}

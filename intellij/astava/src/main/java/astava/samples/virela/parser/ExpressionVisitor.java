package astava.samples.virela.parser;

public interface ExpressionVisitor {
    void visitIntStream();
    void visitId(String id);
    void visitIntLiteral(int value);

    public static final int BINARY_OPERATOR_MUL = 0;
    public static final int BINARY_OPERATOR_DIV = 1;
    void visitBinary(int operator, Expression lhs, Expression rhs);
}

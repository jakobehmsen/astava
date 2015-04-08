package astava.samples.virela.parser;

import java.math.BigDecimal;

public interface ExpressionVisitor {
    //void visitNumberStream();
    void visitId(String id);
    void visitNumberLiteral(BigDecimal value);

    public static final int BINARY_OPERATOR_ADD = 0;
    public static final int BINARY_OPERATOR_SUB = 1;
    public static final int BINARY_OPERATOR_MUL = 2;
    public static final int BINARY_OPERATOR_DIV = 3;
    void visitBinary(int operator, Expression lhs, Expression rhs);
}

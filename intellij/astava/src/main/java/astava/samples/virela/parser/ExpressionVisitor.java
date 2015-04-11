package astava.samples.virela.parser;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/*

l = Line {
    from = {x=5 y=1}
    to = {x=5 y=2}
}

// Wrap first value of right hand side into singleton
// Eagerly evaluate right hand side and wrap value into singleton
l.from.x := l.from.x + 1

l.from.x = mouse.x





t = ConsoleOutPrint {
    text = l.from.x.toString()
}

*/

public interface ExpressionVisitor {
    void visitDict(List<Map.Entry<String, Expression>> entries);
    void visitLookup(Expression target, String id);
    void visitId(String id);
    void visitNumberLiteral(BigDecimal value);
    void visitConstruction(String id, List<Map.Entry<String, Expression>> arguments);

    public static final int BINARY_OPERATOR_ADD = 0;
    public static final int BINARY_OPERATOR_SUB = 1;
    public static final int BINARY_OPERATOR_MUL = 2;
    public static final int BINARY_OPERATOR_DIV = 3;
    void visitBinary(int operator, Expression lhs, Expression rhs);
}

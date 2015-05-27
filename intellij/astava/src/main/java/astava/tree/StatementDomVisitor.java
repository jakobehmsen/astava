package astava.tree;

import java.util.List;
import java.util.Map;

public interface StatementDomVisitor {
    void visitVariableDeclaration(String type, String name);

    void visitVariableAssignment(String name, ExpressionDom value);

    void visitIncrement(String name, int amount);

    void visitReturnValue(ExpressionDom expression);

    void visitBlock(List<StatementDom> statements);

    void visitIfElse(ExpressionDom condition, StatementDom ifTrue, StatementDom ifFalse);

    void visitBreakCase();

    void visitReturn();

    void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments);

    void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments);

    void visitLabel(String name);

    void visitGoTo(String name);

    void visitSwitch(ExpressionDom expression, Map<Integer, StatementDom> cases, StatementDom defaultBody);
}

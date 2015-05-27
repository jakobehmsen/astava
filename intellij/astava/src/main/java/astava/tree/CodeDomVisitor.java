package astava.tree;

public interface CodeDomVisitor {
    void visitStatement(StatementDom statementDom);
    void visitExpression(ExpressionDom expressionDom);
}

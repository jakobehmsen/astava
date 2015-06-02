package astava.java.parser;

public interface DomBuilderVisitor {
    void visitClassBuilder(ClassDomBuilder classBuilder);
    void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder);
    void visitFieldBuilder(FieldDomBuilder fieldBuilder);
    void visitMethodBuilder(MethodDomBuilder methodBuilder);
    void visitStatementBuilder(StatementDomBuilder statementBuilder);
}

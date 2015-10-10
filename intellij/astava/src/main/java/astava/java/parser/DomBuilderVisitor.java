package astava.java.parser;

public interface DomBuilderVisitor {
    void visitClassBuilder(ClassDomBuilder classBuilder);
    void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder);
    void visitFieldBuilder(FieldDomBuilder fieldBuilder);
    void visitMethodBuilder(MethodDomBuilder methodBuilder);
    void visitStatementBuilder(StatementDomBuilder statementBuilder);
    void visitInitializer(StatementDomBuilder statement);

    abstract class Return<T> implements DomBuilderVisitor {
        private T result;

        public void setResult(T result) {
            this.result = result;
        }

        public T visit(DomBuilder domBuilder) {
            domBuilder.accept(this);
            return result;
        }
    }
}

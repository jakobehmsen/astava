package astava.java.parser;

import java.util.Map;

public interface DomBuilderVisitor {
    void visitClassBuilder(ClassDomBuilder classBuilder);
    void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder);
    void visitFieldBuilder(FieldDomBuilder fieldBuilder);
    void visitMethodBuilder(MethodDomBuilder methodBuilder);
    void visitStatementBuilder(StatementDomBuilder statementBuilder);
    void visitInitializer(StatementDomBuilder statement);
    void visitAnnotation(String typeName, Map<String, Object> values);

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

package astava.java.parser;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface DomBuilderVisitor {
    void visitClassBuilder(ClassDomBuilder classBuilder);
    void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder);
    void visitFieldBuilder(FieldDomBuilder fieldBuilder);
    void visitMethodBuilder(MethodDomBuilder methodBuilder);
    void visitStatementBuilder(StatementDomBuilder statementBuilder);
    void visitInitializer(StatementDomBuilder statement);
    void visitAnnotation(UnresolvedType type, Map<String, Function<ClassResolver, Object>> values);
    void visitImplements(List<UnresolvedType> types);

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

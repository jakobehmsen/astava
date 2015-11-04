package astava.java.parser;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class DefaultDomBuilderVisitor implements DomBuilderVisitor {
    @Override
    public void visitClassBuilder(ClassDomBuilder classBuilder) {

    }

    @Override
    public void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder) {

    }

    @Override
    public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {

    }

    @Override
    public void visitMethodBuilder(MethodDomBuilder methodBuilder) {

    }

    @Override
    public void visitStatementBuilder(StatementDomBuilder statementBuilder) {

    }

    @Override
    public void visitInitializer(StatementDomBuilder statement) {

    }

    @Override
    public void visitAnnotation(UnresolvedType type, Map<String, Function<ClassResolver, Object>> values) {

    }

    @Override
    public void visitImplements(List<UnresolvedType> types) {

    }

    public static abstract class Return<T> extends DomBuilderVisitor.Return<T> {
        private T result;

        public void setResult(T result) {
            this.result = result;
        }

        public T visit(DomBuilder domBuilder) {
            domBuilder.accept(this);
            return result;
        }

        @Override
        public void visitClassBuilder(ClassDomBuilder classBuilder) {

        }

        @Override
        public void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder) {

        }

        @Override
        public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {

        }

        @Override
        public void visitMethodBuilder(MethodDomBuilder methodBuilder) {

        }

        @Override
        public void visitStatementBuilder(StatementDomBuilder statementBuilder) {

        }

        @Override
        public void visitInitializer(StatementDomBuilder statement) {

        }

        @Override
        public void visitAnnotation(UnresolvedType type, Map<String, Function<ClassResolver, Object>> values) {

        }

        @Override
        public void visitImplements(List<UnresolvedType> types) {

        }
    }
}

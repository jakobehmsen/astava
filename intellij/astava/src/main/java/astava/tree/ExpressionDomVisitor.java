package astava.tree;

import java.util.List;
import java.util.function.BiFunction;

public interface ExpressionDomVisitor {
    void visitBooleanLiteral(boolean value);

    void visitByteLiteral(byte value);

    void visitShortLiteral(short value);

    void visitIntLiteral(int value);

    void visitLongLiteral(long value);

    void visitFloatLiteral(float value);

    void visitDoubleLiteral(double value);

    void visitCharLiteral(char value);

    void visitStringLiteral(String value);

    void visitArithmetic(int operator, ExpressionDom lhs, ExpressionDom rhs);

    void visitShift(int operator, ExpressionDom lhs, ExpressionDom rhs);

    void visitBitwise(int operator, ExpressionDom lhs, ExpressionDom rhs);

    void visitCompare(int operator, ExpressionDom lhs, ExpressionDom rhs);

    void visitLogical(int operator, ExpressionDom lhs, ExpressionDom rhs);

    void visitVariableAccess(String name);

    void visitFieldAccess(ExpressionDom target, String name, String fieldTypeName);

    void visitStaticFieldAccess(String typeName, String name, String fieldTypeName);

    void visitNot(ExpressionDom expression);

    void visitInstanceOf(ExpressionDom expression, String type);

    void visitBlock(List<CodeDom> codeList);

    void visitIfElse(ExpressionDom condition, ExpressionDom ifTrue, ExpressionDom ifFalse);

    void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments);

    void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments);

    void visitThis();

    void visitNull();

    void visitTop(ExpressionDom expression, BiFunction<ExpressionDom, ExpressionDom, ExpressionDom> usage);

    void visitDup(String type);

    void visitLetBe(String type);

    public static abstract class Return<T> implements ExpressionDomVisitor {
        private T result;

        public void setResult(T result) {
            this.result = result;
        }


        public T returnFrom(ExpressionDom expression) {
            if(expression == null)
                new String();

            expression.accept(this);

            return result;
        }
    }
}

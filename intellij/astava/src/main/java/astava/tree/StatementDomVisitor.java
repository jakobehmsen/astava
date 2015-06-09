package astava.tree;

import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;

public interface StatementDomVisitor {
    void visitVariableDeclaration(String type, String name);

    void visitVariableAssignment(String name, ExpressionDom value);

    void visitFieldAssignment(ExpressionDom target, String name, String type, ExpressionDom value);

    void visitStaticFieldAssignment(String typeName, String name, String type, ExpressionDom value);

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

    void visitASM(MethodNode methodNode);

    class Default implements StatementDomVisitor {
        @Override
        public void visitVariableDeclaration(String type, String name) {

        }

        @Override
        public void visitVariableAssignment(String name, ExpressionDom value) {

        }

        @Override
        public void visitFieldAssignment(ExpressionDom target, String name, String type, ExpressionDom value) {

        }

        @Override
        public void visitStaticFieldAssignment(String typeName, String name, String type, ExpressionDom value) {

        }

        @Override
        public void visitIncrement(String name, int amount) {

        }

        @Override
        public void visitReturnValue(ExpressionDom expression) {

        }

        @Override
        public void visitBlock(List<StatementDom> statements) {

        }

        @Override
        public void visitIfElse(ExpressionDom condition, StatementDom ifTrue, StatementDom ifFalse) {

        }

        @Override
        public void visitBreakCase() {

        }

        @Override
        public void visitReturn() {

        }

        @Override
        public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {

        }

        @Override
        public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {

        }

        @Override
        public void visitLabel(String name) {

        }

        @Override
        public void visitGoTo(String name) {

        }

        @Override
        public void visitSwitch(ExpressionDom expression, Map<Integer, StatementDom> cases, StatementDom defaultBody) {

        }

        @Override
        public void visitASM(MethodNode methodNode) {

        }
    }

    public static abstract class Return<T> extends Default {
        private T result;

        public void setResult(T result) {
            this.result = result;
        }

        public T returnFrom(StatementDom statement) {
            statement.accept(this);
            return result;
        }
    }
}

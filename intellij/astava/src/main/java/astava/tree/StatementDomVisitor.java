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

    void visitMethodBody();

    void visitThrow(ExpressionDom expression);

    void visitTryCatch(StatementDom tryBlock, List<CodeDom> catchBlocks);

    void visitMark(Object label);

    void visitGoTo(Object label);

    void visitArrayStore(ExpressionDom expression, ExpressionDom index, ExpressionDom value);

    void visitSwitch(ExpressionDom expression, Object dflt, int[] keys, Object[] labels);

    void visitIfJump(ExpressionDom condition, Object label);

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

        @Override
        public void visitMethodBody() {

        }

        @Override
        public void visitThrow(ExpressionDom expression) {

        }

        @Override
        public void visitTryCatch(StatementDom tryBlock, List<CodeDom> catchBlocks) {

        }

        @Override
        public void visitMark(Object label) {

        }

        @Override
        public void visitGoTo(Object label) {

        }

        @Override
        public void visitArrayStore(ExpressionDom expression, ExpressionDom index, ExpressionDom value) {

        }

        @Override
        public void visitSwitch(ExpressionDom expression, Object dflt, int[] keys, Object[] labels) {

        }

        @Override
        public void visitIfJump(ExpressionDom condition, Object label) {

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

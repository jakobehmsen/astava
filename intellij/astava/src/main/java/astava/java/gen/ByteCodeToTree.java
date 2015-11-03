package astava.java.gen;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.tree.DefaultExpressionDomVisitor;
import astava.tree.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ByteCodeToTree extends InstructionAdapter {
    private AsStatementOrExpression asStatementOrExpression;

    private interface AsStatementOrExpression {
        StatementDom toStatement();
        ExpressionDom toExpression();
    }

    // Multiple stacks for each branch?
    private MethodNode methodNode;
    private Type returnType;
    private Stack<ExpressionDom> stack = new Stack<>();
    private ArrayList<StatementDom> statements = new ArrayList<>();
    private Hashtable<Integer, String> varToName = new Hashtable<>();

    public ByteCodeToTree(MethodNode methodNode) {
        super(Opcodes.ASM5, new MethodVisitor(Opcodes.ASM5, null) {
        });
        this.methodNode = methodNode;
        this.returnType = Type.getReturnType(methodNode.desc);
    }

    private void checkDecideStatementOrExpression() {
        /*if(asStatementOrExpression != null) {
            stack.add(asStatementOrExpression.toExpression());
            asStatementOrExpression = null;
        }*/
    }

    public void prepareVariables(Consumer<MethodVisitor> accepter) {
        accepter.accept(new MethodVisitor(Opcodes.ASM5) {
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                if(index > 0) {
                    varToName.put(index, name);
                    if(index > Type.getArgumentTypes(methodNode.desc).length)
                        statements.add(DomFactory.declareVar(desc, name));
                }
            }
        });
    }

    @Override
    public void visitInsn(int i) {
        checkDecideStatementOrExpression();

        super.visitInsn(i);
    }

    @Override
    public void visitTypeInsn(int i, String s) {
        checkDecideStatementOrExpression();

        super.visitTypeInsn(i, s);
    }

    @Override
    public void visitIntInsn(int i, int i1) {
        checkDecideStatementOrExpression();

        super.visitIntInsn(i, i1);
    }

    @Override
    public void visitFieldInsn(int i, String s, String s1, String s2) {
        checkDecideStatementOrExpression();

        super.visitFieldInsn(i, s, s1, s2);
    }

    @Override
    public void visitIincInsn(int i, int i1) {
        checkDecideStatementOrExpression();

        super.visitIincInsn(i, i1);
    }

    @Override
    public void visitInvokeDynamicInsn(String s, String s1, Handle handle, Object... objects) {
        checkDecideStatementOrExpression();

        super.visitInvokeDynamicInsn(s, s1, handle, objects);
    }

    @Override
    public void visitJumpInsn(int i, Label label) {
        checkDecideStatementOrExpression();

        super.visitJumpInsn(i, label);
    }

    @Override
    public void visitLdcInsn(Object o) {
        checkDecideStatementOrExpression();

        super.visitLdcInsn(o);
    }

    @Override
    public void visitMethodInsn(int i, String s, String s1, String s2, boolean b) {
        checkDecideStatementOrExpression();

        super.visitMethodInsn(i, s, s1, s2, b);
    }

    @Override
    public void visitVarInsn(int i, int i1) {
        checkDecideStatementOrExpression();

        super.visitVarInsn(i, i1);
    }

    @Override
    public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {
        checkDecideStatementOrExpression();

        super.visitLookupSwitchInsn(label, ints, labels);
    }

    @Override
    public void visitTableSwitchInsn(int i, int i1, Label label, Label... labels) {
        checkDecideStatementOrExpression();

        super.visitTableSwitchInsn(i, i1, label, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String s, int i) {
        checkDecideStatementOrExpression();

        super.visitMultiANewArrayInsn(s, i);
    }

    @Override
    public void iconst(int cst) {
        stack.push(DomFactory.literal(cst));
    }

    @Override
    public void invokevirtual(String owner, String name, String desc, boolean itf) {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        List<ExpressionDom> arguments =
            Arrays.asList(argumentTypes).stream().map(x -> stack.pop()).collect(Collectors.toList());
        ExpressionDom target = stack.pop();
        if(Type.getReturnType(desc).equals(Type.VOID_TYPE))
            statements.add(DomFactory.invokeVirtual(owner, name, desc, target, arguments));
        else
            stack.push(DomFactory.invokeVirtualExpr(owner, name, desc, target, arguments));
    }

    @Override
    public void invokespecial(String owner, String name, String desc, boolean itf) {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        List<ExpressionDom> arguments =
            Arrays.asList(argumentTypes).stream().map(x -> stack.pop()).collect(Collectors.toList());
        ExpressionDom target = stack.pop();

        if(Type.getReturnType(desc).equals(Type.VOID_TYPE))
            statements.add(DomFactory.invokeSpecial(owner, name, desc, target, arguments));
        else
            stack.push(DomFactory.invokeVirtualExpr(owner, name, desc, target, arguments));
    }

    @Override
    public void aconst(Object cst) {
        if(cst instanceof String) {
            stack.push(DomFactory.literal((String)cst));
        } else {
            // What?
        }
    }

    @Override
    public void add(Type type) {
        ExpressionDom rhs = stack.pop();
        ExpressionDom lhs = stack.pop();
        stack.push(DomFactory.add(lhs, rhs));
    }

    @Override
    public void pop() {
        // An expression as statement? Always? When not?
        ExpressionDom expression = stack.pop();

        // E.g. invocation expressions are translated into their statement counterpart
        statements.add(expressionToStatement(expression));
    }

    @Override
    public void getstatic(String owner, String name, String desc) {
        // desc: LClassName; or primitive

        String typeDescriptor = Descriptor.getFieldDescriptorTypeDescriptor(desc);

        stack.push(DomFactory.accessStaticField(owner, name, Type.getType(typeDescriptor).getDescriptor()));
    }

    @Override
    public void putfield(String owner, String name, String desc) {
        // If getfield rightafter (i.e. last statement is putfield), then convert into putfield expression
        ExpressionDom value = stack.pop();
        ExpressionDom target = stack.pop();
        statements.add(DomFactory.assignField(target, name, desc, value));
    }

    @Override
    public void getfield(String owner, String name, String desc) {
        // desc: LClassName; or primitive

        ExpressionDom target = stack.pop();
        String typeDescriptor = Descriptor.getFieldDescriptorTypeDescriptor(desc);

        stack.push(DomFactory.accessField(target, name, Type.getType(typeDescriptor).getDescriptor()));
    }

    @Override
    public void putstatic(String owner, String name, String desc) {

    }

    private String getVarName(int var, String type) {
        return varToName.computeIfAbsent(var, v -> {
            // Use consistent strategies to derive argument- and variable names
            int parameterCount = Type.getArgumentTypes(methodNode.desc).length;
            if((var - 1) < parameterCount)
                return methodNode.parameters != null ? (String)methodNode.parameters.get(var - 1) : "arg" + (var - 1);
            else {
                String name = "var" + (var - parameterCount - 1);
                statements.add(DomFactory.declareVar(type, name));
                return name;
            }
        });
    }

    @Override
    public void load(int var, Type type) {
        if(Modifier.isStatic(methodNode.access)) {

        } else {
            if(var == 0) {
                stack.push(DomFactory.self());
            } else {
                String name = getVarName(var, type.getDescriptor());
                stack.push(DomFactory.accessVar(name));
            }
        }
    }

    @Override
    public void aload(Type type) {
        super.aload(type);
    }

    @Override
    public void store(int var, Type type) {
        if(Modifier.isStatic(methodNode.access)) {

        } else {
            if(var == 0) {
                //stack.push(DomFactory.self());
            } else {
                String name = getVarName(var, type.getDescriptor());
                ExpressionDom value = stack.pop();
                statements.add(DomFactory.assignVar(name, value));
            }
        }
    }

    @Override
    public void astore(Type type) {

    }

    private StatementDom expressionToStatement(ExpressionDom expression) {
        return Util.returnFrom(null, r -> expression.accept(new DefaultExpressionDomVisitor() {
            @Override
            public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {
                r.accept(DomFactory.invoke(invocation, type, name, descriptor, target, arguments));
            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
                r.accept(DomFactory.newInstance(type, parameterTypes, arguments));
            }
        }));
    }

    @Override
    public void areturn(Type type) {
        if(type.equals(Type.VOID_TYPE)) {
            statements.add(DomFactory.ret());
        } else {
            ExpressionDom expression = stack.pop();

            expression = ensureType(returnType, expression);

            statements.add(DomFactory.ret(expression));
        }
    }

    private ExpressionDom ensureType(Type type, ExpressionDom expression) {
        if(type.equals(Type.BOOLEAN_TYPE)) {
            return Util.returnFrom(expression, r -> expression.accept(new DefaultExpressionDomVisitor() {
                @Override
                public void visitIntLiteral(int value) {
                    r.accept(DomFactory.literal(value == 1 ? true : false));
                }
            }));
        } else if(type.equals(Type.BYTE_TYPE)) {
            return Util.returnFrom(expression, r -> expression.accept(new DefaultExpressionDomVisitor() {
                @Override
                public void visitIntLiteral(int value) {
                    r.accept(DomFactory.literal((byte)value));
                }
            }));
        } else if(type.equals(Type.SHORT_TYPE)) {
            return Util.returnFrom(expression, r -> expression.accept(new DefaultExpressionDomVisitor() {
                @Override
                public void visitIntLiteral(int value) {
                    r.accept(DomFactory.literal((short)value));
                }
            }));
        }

        return expression;
    }

    public StatementDom getBlock() {
        return DomFactory.block(statements);
    }
}

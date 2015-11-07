package astava.java.gen;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.java.RelationalOperator;
import astava.tree.DefaultExpressionDomVisitor;
import astava.tree.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ByteCodeToTree extends InstructionAdapter {
    private static class LocalFrame {
        public static final int STATE_UNCONDITIONAL = 0;
        public static final int STATE_IF_TRUE = 1;
        public static final int STATE_IF_FALSE = 2;

        public int state = STATE_UNCONDITIONAL;
        public Stack<ExpressionDom> stack = new Stack<>();
        public ArrayList<StatementDom> statements = new ArrayList<>();

        public ExpressionDom condition;
        public Label ifFalseStart;
        public StatementDom ifTrue;
        public Label endOfIfElse;
    }

    private MethodNode methodNode;
    private Type returnType;
    /*private Stack<ExpressionDom> stack = new Stack<>();
    private ArrayList<StatementDom> statements = new ArrayList<>();*/
    private Hashtable<Integer, String> varToName = new Hashtable<>();
    private Stack<LocalFrame> localFrames = new Stack<>();
    private LocalFrame root;

    public ByteCodeToTree(MethodNode methodNode) {
        super(Opcodes.ASM5, new MethodVisitor(Opcodes.ASM5, null) {
        });
        this.methodNode = methodNode;
        this.returnType = Type.getReturnType(methodNode.desc);

        root = new LocalFrame();
        localFrames.push(root);
        //localFrames.push(new LocalFrame());
    }

    private void checkDecideStatementOrExpression() {
        /*if(asStatementOrExpression != null) {
            stack.add(asStatementOrExpression.toExpression());
            asStatementOrExpression = null;
        }*/
    }

    public Stack<ExpressionDom> getStack() {
        return localFrames.peek().stack;
    }

    public List<StatementDom> getStatements() {
        return localFrames.peek().statements;
    }

    public void prepareVariables(Consumer<MethodVisitor> accepter) {
        accepter.accept(new MethodVisitor(Opcodes.ASM5) {
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                if (index > 0) {
                    if (!varToName.containsKey(index)) {
                        varToName.put(index, name);
                        if (index > Type.getArgumentTypes(methodNode.desc).length)
                            getStatements().add(DomFactory.declareVar(desc, name));
                    }
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
        getStack().push(DomFactory.literal(cst));
    }

    @Override
    public void invokevirtual(String owner, String name, String desc, boolean itf) {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        List<ExpressionDom> arguments =
            Arrays.asList(argumentTypes).stream().map(x -> getStack().pop()).collect(Collectors.toList());
        ExpressionDom target = getStack().pop();
        if(Type.getReturnType(desc).equals(Type.VOID_TYPE))
            getStatements().add(DomFactory.invokeVirtual(owner, name, desc, target, arguments));
        else
            getStack().push(DomFactory.invokeVirtualExpr(owner, name, desc, target, arguments));
    }

    @Override
    public void invokespecial(String owner, String name, String desc, boolean itf) {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        List<ExpressionDom> arguments =
            Arrays.asList(argumentTypes).stream().map(x -> getStack().pop()).collect(Collectors.toList());
        ExpressionDom target = getStack().pop();

        if(Type.getReturnType(desc).equals(Type.VOID_TYPE))
            getStatements().add(DomFactory.invokeSpecial(owner, name, desc, target, arguments));
        else
            getStack().push(DomFactory.invokeVirtualExpr(owner, name, desc, target, arguments));
    }

    @Override
    public void aconst(Object cst) {
        if(cst instanceof String) {
            getStack().push(DomFactory.literal((String) cst));
        } else {
            // What?
        }
    }

    @Override
    public void add(Type type) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        getStack().push(DomFactory.add(lhs, rhs));
    }

    @Override
    public void pop() {
        // An expression as statement? Always? When not?
        ExpressionDom expression = getStack().pop();

        // E.g. invocation expressions are translated into their statement counterpart
        getStatements().add(expressionToStatement(expression));
    }

    @Override
    public void getstatic(String owner, String name, String desc) {
        // desc: LClassName; or primitive

        String typeDescriptor = Descriptor.getFieldDescriptorTypeDescriptor(desc);

        getStack().push(DomFactory.accessStaticField(owner, name, Type.getType(typeDescriptor).getDescriptor()));
    }

    @Override
    public void putfield(String owner, String name, String desc) {
        // If getfield rightafter (i.e. last statement is putfield), then convert into putfield expression
        ExpressionDom value = getStack().pop();
        ExpressionDom target = getStack().pop();
        getStatements().add(DomFactory.assignField(target, name, desc, value));
    }

    @Override
    public void getfield(String owner, String name, String desc) {
        // desc: LClassName; or primitive

        ExpressionDom target = getStack().pop();
        String typeDescriptor = Descriptor.getFieldDescriptorTypeDescriptor(desc);

        getStack().push(DomFactory.accessField(target, name, Type.getType(typeDescriptor).getDescriptor()));
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
                getStatements().add(DomFactory.declareVar(type, name));
                return name;
            }
        });
    }

    @Override
    public void load(int var, Type type) {
        if(Modifier.isStatic(methodNode.access)) {

        } else {
            if(var == 0) {
                getStack().push(DomFactory.self());
            } else {
                String name = getVarName(var, type.getDescriptor());
                getStack().push(DomFactory.accessVar(name));
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
                ExpressionDom value = getStack().pop();
                getStatements().add(DomFactory.assignVar(name, value));
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
            getStatements().add(DomFactory.ret());
        } else {
            ExpressionDom expression = getStack().pop();

            expression = ensureType(returnType, expression);

            getStatements().add(DomFactory.ret(expression));
        }

        LocalFrame frame = localFrames.peek();

        /*
        What about

        if(someCondition) {
            trueBlock
            return ...;
        }

        return ...;
        */
        switch (frame.state) {
            case LocalFrame.STATE_IF_TRUE:
                frame.ifTrue = DomFactory.block(frame.statements);
                frame.statements = new ArrayList<>();
                frame.state = LocalFrame.STATE_IF_FALSE;

                // Should pop?
                break;
            case LocalFrame.STATE_IF_FALSE: {
               /* if(frame.ifFalseStart == label) {
                    frame.ifTrueStatements = frame.statements;
                    frame.statements = new ArrayList<>();
                    frame.state = LocalFrame.STATE_IF_FALSE;
                }*/
                // At end of false block
                // Construct if-else-statement?

                StatementDom ifFalse = DomFactory.block(frame.statements);
                frame.statements = new ArrayList<>();

                // Build if-else-statement
                getStatements().add(DomFactory.ifElse(frame.condition, frame.ifTrue, ifFalse));
                LocalFrame poppedFrame = localFrames.pop();
                localFrames.peek().statements.addAll(poppedFrame.statements);
                break;
        } default: {
                /*LocalFrame poppedFrame = localFrames.pop();
                localFrames.peek().statements.addAll(poppedFrame.statements);*/
                break;
            }
        }
    }

    @Override
    public void ifnull(Label label) {
        ExpressionDom rhs = DomFactory.nil();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
    }

    @Override
    public void ifnonnull(Label label) {
        ExpressionDom rhs = DomFactory.nil();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
    }

    @Override
    public void ifacmpeq(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
    }

    @Override
    public void ifacmpne(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
    }

    @Override
    public void ificmpeq(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
    }

    @Override
    public void ificmpne(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
    }

    @Override
    public void ificmplt(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.GE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.LT), label);
    }

    @Override
    public void ificmpge(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.LT), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.GE), label);
    }

    @Override
    public void ificmpgt(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.LE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.GT), label);
    }

    @Override
    public void ificmple(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.GT), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.LE), label);
    }

    private Hashtable<Label, Object> asmLabelToAstLabelMap = new Hashtable<>();

    private void branch(ExpressionDom condition, Label label) {
        /*
        Created if statement with goto label statement for true block and empty false block
        */

        Object astLabel = asmLabelToAstLabelMap.computeIfAbsent(label, l -> new Object());

        getStatements().add(DomFactory.ifElse(condition, DomFactory.goTo(astLabel), DomFactory.block(Arrays.asList())));
        labelUsages.add(astLabel);

        /*LocalFrame branchFrame = new LocalFrame();
        branchFrame.state = LocalFrame.STATE_IF_TRUE;
        branchFrame.condition = condition;
        branchFrame.ifFalseStart = label;

        localFrames.push(branchFrame);*/
    }

    @Override
    public void goTo(Label label) {
        /*
        Create goto statement
        */

        Object astLabel = asmLabelToAstLabelMap.computeIfAbsent(label, l -> new Object());

        getStatements().add(DomFactory.goTo(astLabel));
        labelUsages.add(astLabel);

        /*LocalFrame frame = localFrames.peek();

        switch (frame.state) {
            case LocalFrame.STATE_IF_TRUE:
                frame.endOfIfElse = label;
                break;
        }*/
    }

    private ArrayList<Object> labelUsages = new ArrayList<>();
    private ArrayList<Runnable> labelUsageChecks = new ArrayList<>();

    @Override
    public void visitLabel(Label label) {
        /*
        Create mark statement
        */

        Object astLabel = asmLabelToAstLabelMap.computeIfAbsent(label, l -> new Object());

        StatementDom mark = DomFactory.mark(astLabel);
        getStatements().add(mark);

        labelUsageChecks.add(new Runnable() {
            @Override
            public void run() {
                if(!labelUsages.contains(astLabel)) {
                    int index = IntStream.range(0, getStatements().size())
                        .filter(i -> getStatements().get(i) == mark)
                        .findAny().getAsInt();
                    getStatements().remove(index);
                    //getStatements().remove(mark);
                }
            }
        });

        /*if(localFrames.size() == 0)
            return;

        LocalFrame frame = localFrames.peek();

        switch (frame.state) {
            case LocalFrame.STATE_IF_TRUE:
                if(frame.ifFalseStart == label) {
                    frame.ifTrue = DomFactory.block(frame.statements);

                    if(frame.endOfIfElse != null) {
                        // True block of if-else-statement
                        frame.statements = new ArrayList<>();
                        frame.state = LocalFrame.STATE_IF_FALSE;
                    } else {
                        // True block of if-statement
                        frame.statements = new ArrayList<>();

                        // Build if-statement
                        // Create special if-statement?
                        getStatements().add(DomFactory.ifElse(frame.condition, frame.ifTrue, DomFactory.block(Arrays.asList())));

                        LocalFrame poppedFrame = localFrames.pop();
                        localFrames.peek().statements.addAll(poppedFrame.statements);
                    }
                }
                break;
            case LocalFrame.STATE_IF_FALSE:
                if(frame.endOfIfElse == label) {
                    StatementDom ifFalse = DomFactory.block(frame.statements);
                    frame.statements = new ArrayList<>();

                    // Build if-else-statement
                    getStatements().add(DomFactory.ifElse(frame.condition, frame.ifTrue, ifFalse));

                    LocalFrame poppedFrame = localFrames.pop();
                    localFrames.peek().statements.addAll(poppedFrame.statements);
                }
                break;
        }*/

        // If at else block, then extract true statements into block
        // True statements ended with an unconditional jump, that label is interpreted as the end of the if-else-statement
        // The construction of such a statement is postponed till the label for the jump is met.
        // If true statements didn't end with such as jump, then it is interpreted as if the true statements represents
        // an if-statement - and such a statement can be constructed immediately.

        // If at end of if-else-statement label, true statements and false statements can be constructed and an
        // if-else-statement can then be constructed
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
        labelUsageChecks.forEach(x -> x.run());

        return DomFactory.block(root.statements);
    }
}

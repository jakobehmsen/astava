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
        public Stack<ExpressionDom> originalStack;
        public Stack<ExpressionDom> stack;
        public Stack<ExpressionDom> stackIfTrue;
        public ArrayList<StatementDom> statements = new ArrayList<>();
        public Label jumpLabel;
        public int ifTrueStart;
        public int ifTrueEnd;
        public int ifFalseStart;
        public int ifFalseEnd;

        public ExpressionDom conditionNegative;
        //public Label ifFalseStart;
        public StatementDom ifTrue;
        public Label endOfIfElse;
        public Label lastGoToLabel;
        public Label branchEnd;
        public ArrayList<Object> labelUsages = new ArrayList<>();
        public ArrayList<Runnable> labelUsageChecks = new ArrayList<>();
        public ExpressionDom conditionPositive;
        public ArrayList<LocalFrame> mergedBranches = new ArrayList<>();
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
        root.stack = new Stack<>();
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

        //LocalFrame frame = localFrames.peek();

        /*
        What about

        if(someCondition) {
            trueBlock
            return ...;
        }

        return ...;
        */

        if (localFrames.size() > 1) {
            /*LocalFrame poppedFrame = localFrames.pop();
            localFrames.peek().statements.addAll(poppedFrame.statements);*/

            popFrame();
        }

        /*switch (frame.state) {
            case LocalFrame.STATE_IF_TRUE:
                frame.ifTrue = DomFactory.block(frame.statements);
                frame.statements = new ArrayList<>();
                frame.state = LocalFrame.STATE_IF_FALSE;

                // Should pop?
                break;
            case LocalFrame.STATE_IF_FALSE: {
                // At end of false block
                // Construct if-else-statement?

                StatementDom ifFalse = DomFactory.block(frame.statements);
                frame.statements = new ArrayList<>();

                // Build if-else-statement
                getStatements().add(DomFactory.ifElse(frame.conditionNegative, frame.ifTrue, ifFalse));
                LocalFrame poppedFrame = localFrames.pop();
                localFrames.peek().statements.addAll(poppedFrame.statements);
                break;
        } default: {
                break;
            }
        }*/
    }

    @Override
    public void ifnull(Label label) {
        ExpressionDom rhs = DomFactory.nil();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
    }

    @Override
    public void ifnonnull(Label label) {
        ExpressionDom rhs = DomFactory.nil();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
    }

    @Override
    public void ifacmpeq(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
    }

    @Override
    public void ifacmpne(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
    }

    @Override
    public void ificmpeq(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), DomFactory.compare(lhs, rhs, RelationalOperator.NE), label);
    }

    @Override
    public void ificmpne(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.NE), DomFactory.compare(lhs, rhs, RelationalOperator.EQ), label);
    }

    @Override
    public void ificmplt(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.GE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.LT), DomFactory.compare(lhs, rhs, RelationalOperator.GE), label);
    }

    @Override
    public void ificmpge(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.LT), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.GE), DomFactory.compare(lhs, rhs, RelationalOperator.LT), label);
    }

    @Override
    public void ificmpgt(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.LE), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.GT), DomFactory.compare(lhs, rhs, RelationalOperator.LE), label);
    }

    @Override
    public void ificmple(Label label) {
        ExpressionDom rhs = getStack().pop();
        ExpressionDom lhs = getStack().pop();
        //branch(DomFactory.compare(lhs, rhs, RelationalOperator.GT), label);
        branch(DomFactory.compare(lhs, rhs, RelationalOperator.LE), DomFactory.compare(lhs, rhs, RelationalOperator.GT), label);
    }

    private Hashtable<Label, Object> asmLabelToAstLabelMap = new Hashtable<>();

    private void branch(ExpressionDom conditionNegative, ExpressionDom conditionPositive, Label label) {
        /*
        Created if statement with goto label statement for true block and empty false block
        */

        Object astLabel = getAstLabel(label);;

        /*getStatements().add(DomFactory.ifElse(conditionNegative, DomFactory.goTo(astLabel), DomFactory.block(Arrays.asList())));
        labelUsages.add(astLabel);*/

        //localFrames.peek().lastGoToLabel = label;

        LocalFrame branchFrame = new LocalFrame();
        branchFrame.ifTrueStart = localFrames.peek().statements.size();
        branchFrame.jumpLabel = label;
        branchFrame.originalStack = localFrames.peek().stack;
        branchFrame.stack = (Stack<ExpressionDom>)branchFrame.originalStack.clone();
        branchFrame.conditionNegative = conditionNegative;
        branchFrame.conditionPositive = conditionPositive;

        //branchFrame.conditionNegative = conditionNegative;
        //branchFrame.ifFalseStart = label;

        localFrames.push(branchFrame);

        /*LocalFrame branchFrame = new LocalFrame();
        branchFrame.state = LocalFrame.STATE_IF_TRUE;
        branchFrame.conditionNegative = conditionNegative;
        branchFrame.ifFalseStart = label;

        localFrames.push(branchFrame);*/
    }

    @Override
    public void goTo(Label label) {
        localFrames.peek().lastGoToLabel = label; // Potential end of if-else-code

        /*
        Create goto statement
        */

        Object astLabel = getAstLabel(label);;

        getStatements().add(DomFactory.goTo(astLabel));

        LocalFrame frame = localFrames.peek();

        frame.labelUsages.add(astLabel);

        /*switch (frame.state) {
            case LocalFrame.STATE_IF_TRUE:
                break;
        }*/

        /*LocalFrame frame = localFrames.peek();

        switch (frame.state) {
            case LocalFrame.STATE_IF_TRUE:
                frame.endOfIfElse = label;
                break;
        }*/
    }

    //private ArrayList<Object> labelUsages = new ArrayList<>();
    //private ArrayList<Runnable> labelUsageChecks = new ArrayList<>();

    private Object getAstLabel(Label asmLabel) {
        return asmLabelToAstLabelMap.computeIfAbsent(asmLabel, l -> new Object());
    }

    @Override
    public void visitLabel(Label label) {
        /*
        Create mark statement
        */

        Object astLabel = getAstLabel(label);

        StatementDom mark = DomFactory.mark(astLabel);
        LocalFrame frame = localFrames.peek();

        if(label == frame.jumpLabel ||
            frame.mergedBranches.stream().anyMatch(x -> label == x.jumpLabel)) {
            frame.ifFalseStart = frame.statements.size();
            frame.branchEnd = frame.lastGoToLabel;
            //frame.stack = (Stack<ExpressionDom>)frame.originalStack.clone();
            frame.stackIfTrue = frame.stack;
            frame.stack = frame.originalStack;
        }

        //boolean mergedBranches = false;

        if(localFrames.size() > 1) {
            LocalFrame outerFrame = localFrames.get(localFrames.size() - 2);
            // Or one of the other merged branches' jumpLabel
            if(label == outerFrame.jumpLabel ||
                outerFrame.mergedBranches.stream().anyMatch(x -> label == x.jumpLabel)) {
                outerFrame.mergedBranches.add(frame);

                //outerFrame.statements.add(mark);

                // Merge branch into outer branch
                popFrame();

                //mergedBranches = true;
            }
        }

        /*if(!mergedBranches) {
            getStatements().add(mark);
        }*/

        getStatements().add(mark);

        if(label == frame.branchEnd) {
            popFrame();
        }

        localFrames.peek().labelUsageChecks.add(new Runnable() {
            @Override
            public void run() {
                if(!localFrames.peek().labelUsages.contains(astLabel)) {
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
                        getStatements().add(DomFactory.ifElse(frame.conditionNegative, frame.ifTrue, DomFactory.block(Arrays.asList())));

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
                    getStatements().add(DomFactory.ifElse(frame.conditionNegative, frame.ifTrue, ifFalse));

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

    private void popFrame() {
        LocalFrame frame = localFrames.pop();

        if(frame.stack.size() == 1 && frame.stackIfTrue.size() == 1 &&
            frame.statements.size() == 2 &&
            frame.statements.get(0).equals(DomFactory.goTo(getAstLabel(frame.lastGoToLabel))) &&
            frame.statements.get(1).equals(DomFactory.mark(getAstLabel(frame.jumpLabel)))) {
            localFrames.peek().stack.add(DomFactory.ifElseExpr(frame.conditionPositive, frame.stackIfTrue.pop(), frame.stack.pop()));
        } else {
            localFrames.peek().statements.add(DomFactory.ifElse(frame.conditionNegative, DomFactory.goTo(getAstLabel(frame.jumpLabel)), DomFactory.block(Arrays.asList())));
            localFrames.peek().labelUsages.add(getAstLabel(frame.jumpLabel));
            localFrames.peek().statements.addAll(frame.statements);
            localFrames.peek().labelUsages.addAll(frame.labelUsages);
            localFrames.peek().labelUsageChecks.addAll(frame.labelUsageChecks);
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
        localFrames.peek().labelUsageChecks.forEach(x ->
            x.run());

        /*List<StatementDom> cleanedStatements = root.statements.stream().filter(x -> {
            return Util.returnFrom(true, r -> x.accept(new DefaultStatementDomVisitor() {
                @Override
                public void visitMark(Object label) {
                    r.accept(localFrames.peek().labelUsages.contains(label));
                }
            }));
        }).collect(Collectors.toList());*/

        //return DomFactory.block(root.statements);

        return DomFactory.block(getStatements());
    }
}

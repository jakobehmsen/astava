package astava.java.gen;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.java.Invocation;
import astava.tree.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// Inspired by: http://www.javacodegeeks.com/2013/12/anatomy-of-a-java-decompiler.html
public class ByteCodeToTree2 extends InstructionAdapter {
    private MethodNode methodNode;
    private Type returnType;
    private Hashtable<Integer, String> varToName = new Hashtable<>();

    private interface ExpressionBuilder {
        default boolean isConstant() {
            return children().isEmpty() || children().stream().anyMatch(x -> x.isConstant());
        }
        default List<ExpressionBuilder> children() { return Arrays.asList();
        }
        ExpressionDom build();
    }

    public StatementDom getBlock() {
        ArrayList<StatementDom> statements = new ArrayList<>();

        statementBuilders.forEach(x ->
            x.accept(statements));

        return DomFactory.block(statements);
    }

    private static class Branch {
        int stackIndex;
        Label jumpLabel;
    }

    private Stack<Branch> branches = new Stack<>();

    public ByteCodeToTree2(MethodNode methodNode) {
        super(Opcodes.ASM5, new MethodVisitor(Opcodes.ASM5, null) {
        });
        this.methodNode = methodNode;
        this.returnType = Type.getReturnType(methodNode.desc);
    }

    private int stackIndex = 0;
    private Stack<List<String>> stack = new Stack<>();
    private int stackVariableNo = 0;
    private Hashtable<String, Integer> varAssignCount = new Hashtable<>();
    private Hashtable<String, ExpressionBuilder> stackVarValues = new Hashtable<>();
    private HashSet<Object> labelUsages = new HashSet<>();
    private ArrayList<Consumer<List<StatementDom>>> statementBuilders = new ArrayList<>();
    private Hashtable<String, String> relabels = new Hashtable<>();

    private boolean isArgument(int index) {
        return index > 0 && index <= Type.getArgumentTypes(methodNode.desc).length;
    }

    public void prepareVariables(Consumer<MethodVisitor> accepter) {
        accepter.accept(new MethodVisitor(Opcodes.ASM5) {
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                if (index > 0) {
                    if (!varToName.containsKey(index)) {
                        varToName.put(index, name);
                    }

                    if(isArgument(index)) {
                        varAssignCount.put(name, 1);
                    }
                }
            }
        });
    }

    @Override
    public void invokevirtual(String owner, String name, String desc, boolean itf) {
        invoke(owner, name, desc, Invocation.VIRTUAL);
    }

    @Override
    public void invokespecial(String owner, String name, String desc, boolean itf) {
        switch (state) {
            case STATE_NEW_INSTANCE:
                Type[] argumentTypes = Type.getArgumentTypes(desc);
                List<String> parameterTypes = Arrays.asList(argumentTypes).stream().map(x -> x.getDescriptor()).collect(Collectors.toList());
                List<ExpressionBuilder> arguments =
                    Arrays.asList(argumentTypes).stream().map(x -> stackPop()).collect(Collectors.toList());
                Collections.reverse(arguments);
                stackPush(() -> DomFactory.newInstanceExpr(owner, parameterTypes, arguments.stream().map(x -> x.build()).collect(Collectors.toList())));
                state = STATE_DEFAULT;

                break;
            default:
                invoke(owner, name, desc, Invocation.SPECIAL);
        }
    }

    @Override
    public void invokeinterface(String owner, String name, String desc) {
        invoke(owner, name, desc, Invocation.INTERFACE);
    }

    @Override
    public void invokestatic(String owner, String name, String desc, boolean itf) {
        invoke(owner, name, desc, Invocation.STATIC);
    }

    private void invoke(String owner, String name, String desc, int invocation) {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        List<ExpressionBuilder> arguments =
            Arrays.asList(argumentTypes).stream().map(x -> stackPop()).collect(Collectors.toList());
        Collections.reverse(arguments);
        ExpressionBuilder target = stackPop();
        if(Type.getReturnType(desc).equals(Type.VOID_TYPE))
            statementBuilders.add(statements ->
                statements.add(DomFactory.invoke(invocation, owner, name, desc, target.build(), arguments.stream().map(x -> x.build()).collect(Collectors.toList()))));
        else
            stackPush(() -> DomFactory.invokeExpr(invocation, owner, name, desc, target.build(), arguments.stream().map(x -> x.build()).collect(Collectors.toList())));
    }

    private final int STATE_DEFAULT = 0;
    private final int STATE_NEW_INSTANCE = 1;
    private int state = STATE_DEFAULT;

    @Override
    public void visitIincInsn(int var, int increment) {
        String name = getVarName(var, Descriptor.get(int.class));

        statementBuilders.add(statements ->
            statements.add(DomFactory.intIncVar(name, increment)));
    }

    @Override
    public void anew(Type type) {
        /*
        NEW java/lang/Object
        DUP
        INVOKESPECIAL java/lang/Object.<init> ()V
        */

        /*
        The bytecode pattern:
        NEW, DUP, INVOKESPECIAL
        should be translated into a single newInstance statement or expression
        This could be implemented by having a state machine
        */

        switch (state) {
            case STATE_DEFAULT:
                state = STATE_NEW_INSTANCE;
                break;
        }
    }

    @Override
    public void dup() {
        switch (state) {
            case STATE_NEW_INSTANCE:
                break;
        }
    }

    @Override
    public void pop() {
        // An expression as statement? Always? When not?
        ExpressionBuilder expression = stackPop();

        // E.g. invocation expressions are translated into their statement counterpart
        statementBuilders.add(statements ->
            statements.add(expressionToStatement(expression.build())));
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
    public void iconst(int i) {
        stackPush(() -> DomFactory.literal(i));
    }

    @Override
    public void fconst(float v) {
        stackPush(() -> DomFactory.literal(v));
    }

    @Override
    public void aconst(Object o) {
        if(o instanceof String) {
            stackPush(() -> DomFactory.literal((String)o));
        } else {

        }
    }

    @Override
    public void dconst(double v) {
        stackPush(() -> DomFactory.literal(v));
    }

    @Override
    public void lconst(long l) {
        stackPush(() -> DomFactory.literal(l));
    }

    @Override
    public void tconst(Type type) {
        stackPush(() -> DomFactory.classLiteral(type.getDescriptor()));
    }

    @Override
    public void add(Type type) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        stackPush(() -> DomFactory.add(lhs.build(), rhs.build()));
    }

    @Override
    public void sub(Type type) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        stackPush(() -> DomFactory.sub(lhs.build(), rhs.build()));
    }

    @Override
    public void mul(Type type) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        stackPush(() -> DomFactory.mul(lhs.build(), rhs.build()));
    }

    @Override
    public void div(Type type) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        stackPush(() -> DomFactory.div(lhs.build(), rhs.build()));
    }

    @Override
    public void rem(Type type) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        stackPush(() -> DomFactory.rem(lhs.build(), rhs.build()));
    }

    @Override
    public void putfield(String owner, String name, String desc) {
        // If getfield rightafter (i.e. last statement is putfield), then convert into putfield expression

        ExpressionBuilder value = stackPop();
        ExpressionBuilder target = stackPop();
        statementBuilders.add(statements -> statements.add(DomFactory.assignField(target.build(), name, desc, value.build())));
    }

    @Override
    public void getfield(String owner, String name, String desc) {
        // desc: LClassName; or primitive

        ExpressionBuilder target = stackPop();
        String typeDescriptor = Descriptor.getFieldDescriptorTypeDescriptor(desc);

        stackPush(() -> DomFactory.accessField(target.build(), name, Type.getType(typeDescriptor).getDescriptor()));
    }

    @Override
    public void ifne(Label label) {
        ExpressionBuilder rhs = () -> DomFactory.literal(false);
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.ne(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ifeq(Label label) {
        ExpressionBuilder rhs = () -> DomFactory.literal(false);
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.eq(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ificmpeq(Label label) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.eq(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ificmpne(Label label) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.ne(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ificmple(Label label) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.lt(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ificmpgt(Label label) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.gt(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ificmpge(Label label) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.ge(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ificmplt(Label label) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.gt(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ifnull(Label label) {
        ExpressionBuilder rhs = () -> DomFactory.nil();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.eq(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ifnonnull(Label label) {
        ExpressionBuilder rhs = () -> DomFactory.nil();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.ne(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ifacmpeq(Label label) {
        ExpressionBuilder rhs = () -> DomFactory.nil();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.eq(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ifacmpne(Label label) {
        ExpressionBuilder rhs = () -> DomFactory.nil();
        ExpressionBuilder lhs = stackPop();
        branch(() -> DomFactory.ne(lhs.build(), rhs.build()), label);
    }

    private void branch(ExpressionBuilder condition, Label jumpLabel) {
        statementBuilders.add(statements -> {
            statements.add(DomFactory.ifElse(condition.build(), DomFactory.goTo(jumpLabel), DomFactory.block(Arrays.asList())));
        });

        Branch b = new Branch();
        b.jumpLabel = jumpLabel;
        b.stackIndex = stack.size();
        branches.push(b);

        labelUsages.add(jumpLabel);
    }

    @Override
    public void areturn(Type type) {
        if(!type.equals(Type.VOID_TYPE)) {
            ExpressionBuilder value = stackPop();

            statementBuilders.add(statements -> {
                ExpressionDom v = value.build();
                v = ensureType(returnType, v);

                statements.add(DomFactory.ret(v));
            });

            if (branches.size() > 0)
                branches.pop();
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

    @Override
    public void visitLabel(Label label) {
        if(branches.size() > 0) {
            if(branches.peek().jumpLabel == label) {
                // Rewind stack index
                stackIndex = branches.peek().stackIndex;
            }
        }

        statementBuilders.add(statements -> {
            if(labelUsages.contains(label))
                statements.add(DomFactory.mark(label));
        });
    }

    @Override
    public void goTo(Label label) {
        statementBuilders.add(statements ->
            statements.add(DomFactory.goTo(label)));

        labelUsages.add(label);
    }

    private String getVarName(int var, String type) {
        return varToName.computeIfAbsent(var, v -> {
            // Use consistent strategies to derive argument- and variable names
            int parameterCount = Type.getArgumentTypes(methodNode.desc).length;
            if((var - 1) < parameterCount)
                return methodNode.parameters != null ? (String)methodNode.parameters.get(var - 1) : "a" + (var - 1);
            else {
                String name = "v" + (var - parameterCount - 1);
                statementBuilders.add(statements ->
                    statements.add(DomFactory.declareVar(type, name)));
                return name;
            }
        });
    }

    @Override
    public void load(int var, Type type) {
        if(Modifier.isStatic(methodNode.access)) {

        } else {
            if(var == 0) {
                stackPush(() -> DomFactory.self());
            } else {
                String name = getVarName(var, type.getDescriptor());

                stackPush(new ExpressionBuilder() {
                    @Override
                    public boolean isConstant() {
                        return varAssignCount.get(name) == 1;
                    }

                    @Override
                    public ExpressionDom build() {
                        return DomFactory.accessVar(name);
                    }
                });
            }
        }
    }

    @Override
    public void store(int var, Type type) {
        if(Modifier.isStatic(methodNode.access)) {

        } else {
            if(var != 0) {
                String name = getVarName(var, type.getDescriptor());

                ExpressionBuilder value = stackPop();
                statementBuilders.add(statements ->
                    statements.add(DomFactory.assignVar(name, value.build())));

                Integer varAssign = varAssignCount.get(name);
                if(varAssign != null)
                    varAssignCount.put(name, varAssign + 1);
                else
                    varAssignCount.put(name, 1);
            }
        }
    }

    private void stackPush(ExpressionBuilder value) {
        String varName = "s" + stackVariableNo++;
        if(stackIndex == stack.size()) {
            stack.push(new ArrayList<>(Arrays.asList(varName)));
        } else {
            List<String> varNames = stack.get(stackIndex);
            varNames.add(varName);
        }
        stackIndex++;
        stackVarValues.put(varName, value);

        statementBuilders.add(statements -> {
            String relabeledName = relabels.get(varName);
            String name = relabeledName != null ? relabeledName : varName;
            if(relabeledName != null || !stackVarValues.get(varName).isConstant()) {
                statements.add(DomFactory.assignVar(name, value.build()));
            }
        });
    }

    private ExpressionBuilder stackPop() {
        List<String> stackVarNames;

        if(stackIndex == stack.size()) {
            stackVarNames = stack.pop();
        } else {
            stackVarNames = stack.get(stackIndex);
        }
        stackIndex--;

        if(stackVarNames.size() == 1) {
            return () -> {
                ExpressionBuilder value = stackVarValues.get(stackVarNames.get(0));

                if(value.isConstant()) {
                    return value.build();
                } else
                    return DomFactory.accessVar(stackVarNames.get(0));
            };
        } else {
            String commonName = stackVarNames.stream().collect(Collectors.joining(", ", "{", "}"));
            stackVarNames.forEach(x -> {
                relabels.put(x, commonName);
            });

            return () -> DomFactory.accessVar(commonName);
        }
    }
}

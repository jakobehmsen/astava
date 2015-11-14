package astava.java.gen;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.tree.ExpressionDom;
import astava.tree.StatementDom;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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

    public void prepareVariables(Consumer<MethodVisitor> accepter) {
        accepter.accept(new MethodVisitor(Opcodes.ASM5) {
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                if (index > 0) {
                    if (!varToName.containsKey(index)) {
                        varToName.put(index, name);
                    }

                    if(index <= Type.getArgumentTypes(methodNode.desc).length) {
                        varAssignCount.put(name, 1);
                    }
                }
            }
        });
    }

    @Override
    public void iconst(int i) {
        stackPush(() -> DomFactory.literal(i));
    }

    @Override
    public void add(Type type) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        stackPush(() -> DomFactory.add(lhs.build(), rhs.build()));
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
        branch2(() -> DomFactory.ne(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ifeq(Label label) {
        ExpressionBuilder rhs = () -> DomFactory.literal(false);
        ExpressionBuilder lhs = stackPop();
        branch2(() -> DomFactory.eq(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ificmpeq(Label label) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        branch2(() -> DomFactory.eq(lhs.build(), rhs.build()), label);
    }

    @Override
    public void ificmpne(Label label) {
        ExpressionBuilder rhs = stackPop();
        ExpressionBuilder lhs = stackPop();
        branch2(() -> DomFactory.ne(lhs.build(), rhs.build()), label);
    }

    private void branch2(ExpressionBuilder condition, Label jumpLabel) {
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
        ExpressionBuilder value = stackPop();

        statementBuilders.add(statements -> {
            statements.add(DomFactory.ret(value.build()));
        });

        if(branches.size() > 0)
            branches.pop();
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

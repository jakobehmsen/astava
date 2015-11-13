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

    public StatementDom getBlock() {
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
    private ArrayList<StatementDom> statements = new ArrayList<>();
    private Hashtable<String, Consumer<String>> stackVarRelabelers = new Hashtable<>();

    public void prepareVariables(Consumer<MethodVisitor> accepter) {
        accepter.accept(new MethodVisitor(Opcodes.ASM5) {
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                if (index > 0) {
                    if (!varToName.containsKey(index)) {
                        varToName.put(index, name);
                    }
                }
            }
        });
    }

    @Override
    public void iconst(int i) {
        stackPush(DomFactory.literal(i));
    }

    @Override
    public void add(Type type) {
        ExpressionDom rhs = stackPop();
        ExpressionDom lhs = stackPop();
        stackPush(DomFactory.add(lhs, rhs));
    }

    @Override
    public void putfield(String owner, String name, String desc) {
        // If getfield rightafter (i.e. last statement is putfield), then convert into putfield expression
        ExpressionDom value = stackPop();
        ExpressionDom target = stackPop();
        statements.add(DomFactory.assignField(target, name, desc, value));
    }

    @Override
    public void getfield(String owner, String name, String desc) {
        // desc: LClassName; or primitive

        ExpressionDom target = stackPop();
        String typeDescriptor = Descriptor.getFieldDescriptorTypeDescriptor(desc);

        stackPush(DomFactory.accessField(target, name, Type.getType(typeDescriptor).getDescriptor()));
    }

    @Override
    public void ificmpeq(Label label) {
        ExpressionDom rhs = stackPop();
        ExpressionDom lhs = stackPop();
        branch(DomFactory.eq(lhs, rhs), label);
    }

    @Override
    public void ificmpne(Label label) {
        ExpressionDom rhs = stackPop();
        ExpressionDom lhs = stackPop();
        branch(DomFactory.ne(lhs, rhs), label);
    }

    private void branch(ExpressionDom condition, Label jumpLabel) {
        statements.add(DomFactory.ifElse(condition, DomFactory.goTo(jumpLabel), DomFactory.block(Arrays.asList())));
        Branch b = new Branch();
        b.jumpLabel = jumpLabel;
        b.stackIndex = stack.size();
        branches.push(b);
    }

    @Override
    public void areturn(Type type) {
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

        statements.add(DomFactory.mark(label));
    }

    @Override
    public void goTo(Label label) {
        statements.add(DomFactory.goTo(label));
    }

    private String getVarName(int var, String type) {
        return varToName.computeIfAbsent(var, v -> {
            // Use consistent strategies to derive argument- and variable names
            int parameterCount = Type.getArgumentTypes(methodNode.desc).length;
            if((var - 1) < parameterCount)
                return methodNode.parameters != null ? (String)methodNode.parameters.get(var - 1) : "a" + (var - 1);
            else {
                String name = "v" + (var - parameterCount - 1);
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
                stackPush(DomFactory.self());
            } else {
                String name = getVarName(var, type.getDescriptor());
                stackPush(DomFactory.accessVar(name));
            }
        }
    }

    @Override
    public void store(int var, Type type) {
        if(Modifier.isStatic(methodNode.access)) {

        } else {
            if(var == 0) {
                //stack.push(DomFactory.self());
            } else {
                String name = getVarName(var, type.getDescriptor());
                ExpressionDom value = stackPop();
                statements.add(DomFactory.assignVar(name, value));
            }
        }
    }

    private void stackPush(ExpressionDom value) {
        String varName = "s" + stackVariableNo++;
        if(stackIndex == stack.size()) {
            stack.push(new ArrayList<>(Arrays.asList(varName)));
        } else {
            List<String> varNames = stack.get(stackIndex);
            varNames.add(varName);
        }
        stackIndex++;
        statements.add(DomFactory.assignVar(varName, value));
        int statementIndex = statements.size();

        stackVarRelabelers.put(
            varName,
            n ->
                statements.set(statementIndex, DomFactory.assignVar(n, value)));
    }

    private ExpressionDom stackPop() {
        List<String> stackVarNames;

        if(stackIndex == stack.size()) {
            stackVarNames = stack.pop();
        } else {
            stackVarNames = stack.get(stackIndex);
        }
        stackIndex--;

        if(stackVarNames.size() == 1) {
            return DomFactory.accessVar(stackVarNames.get(0));
        } else {
            String commonName = stackVarNames.stream().collect(Collectors.joining(", ", "{", "}"));
            stackVarNames.forEach(x -> {
                stackVarRelabelers.get(x).accept(commonName);
            });
            return DomFactory.accessVar(commonName);
        }
    }
}
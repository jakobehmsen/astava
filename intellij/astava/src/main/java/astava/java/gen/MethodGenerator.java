package astava.java.gen;

import astava.java.*;
import astava.tree.*;
import javafx.util.Pair;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.TableSwitchGenerator;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static astava.java.DomFactory.*;

public class MethodGenerator {
    private String thisClassName;
    private StatementDom body;
    //private GenerateScope methodScope;
    private List<ParameterInfo> parameters;

    public MethodGenerator(ClassGenerator classGenerator, List<ParameterInfo> parameters, StatementDom body) {
        this(classGenerator.getClassName(), parameters, body);
    }

    public MethodGenerator(String thisClassName, List<ParameterInfo> parameters, StatementDom body) {
        this.thisClassName = thisClassName;
        this.parameters = parameters;
        this.body = body;
        //this.methodScope = new GenerateScope(outerScope);
    }

    public void generate(MethodNode methodNode) {
        generate(methodNode, methodNode.instructions);
    }

    //public void generate(GeneratorAdapter generator) {
    public void generate(MethodNode methodNode, InsnList originalInstructions) {
        /*LabelScope labelScope = new LabelScope();
        methodNode.visitCode();

        Method m = new Method(methodNode.name, methodNode.desc);
        GeneratorAdapter generator;
        try {
            generator = new GeneratorAdapter(methodNode.access, m, methodNode);
        } catch(Exception e) {
            generator = null;
        }
        populateMethodStatement(methodNode, generator, body, null, labelScope);

        methodNode.visitEnd();
        methodNode.visitMaxs(0, 0);
        labelScope.verify();*/

        generate(methodNode, (mn, generator) -> {
            populateMethodBody(methodNode, originalInstructions, generator);
        });
    }

    public static void generate(MethodNode methodNode, BiConsumer<MethodNode, GeneratorAdapter> bodyGenerator) {
        //LabelScope labelScope = new LabelScope();
        methodNode.visitCode();

        Method m = new Method(methodNode.name, methodNode.desc);
        GeneratorAdapter generator;
        try {
            generator = new GeneratorAdapter(methodNode.access, m, methodNode);
        } catch(Exception e) {
            generator = null;
        }

        bodyGenerator.accept(methodNode, generator);

        methodNode.visitEnd();
        methodNode.visitMaxs(0, 0);
        //labelScope.verify();
    }

    private static class MethodBodyInjection {
        public int occurrences;
        public int withReturn;
    }

    public void populateMethodBody(MethodNode methodNode, InsnList originalInstructions, GeneratorAdapter generator) {
        LabelScope labelScope = new LabelScope();
        MethodBodyInjection injectedMethodBody = new MethodBodyInjection();
        populateMethodStatement(methodNode, originalInstructions, generator, body, null, labelScope, injectedMethodBody, new GenerateScope());
        if(injectedMethodBody.occurrences > 0) {
            generator.returnValue();
        }
        labelScope.verify();
    }

    public String populateMethodStatement(MethodNode methodNode, InsnList originalInstructions, GeneratorAdapter generator, StatementDom statement, Label breakLabel, LabelScope labelScope, MethodBodyInjection methodBodyInjection, GenerateScope scope) {
        statement.accept(new StatementDomVisitor() {
            @Override
            public void visitVariableDeclaration(String type, String name) {
                scope.declareVar(generator, type, name);
            }

            @Override
            public void visitVariableAssignment(String name, ExpressionDom value) {
                String valueType = populateMethodExpression(methodNode, originalInstructions, generator, value, null, true, scope);
                int id = scope.getVarId(name);
                generator.storeLocal(id, Type.getType(valueType));
            }

            @Override
            public void visitFieldAssignment(ExpressionDom target, String name, String type, ExpressionDom value) {
                String targetType = populateMethodExpression(methodNode, originalInstructions, generator, target, null, true, scope);
                String valueType = populateMethodExpression(methodNode, originalInstructions, generator, value, null, true, scope);
                generator.putField(Type.getType(targetType), name, Type.getType(Descriptor.getFieldDescriptor(type)));
            }

            @Override
            public void visitStaticFieldAssignment(String typeName, String name, String type, ExpressionDom value) {
                String valueType = populateMethodExpression(methodNode, originalInstructions, generator, value, null, true, scope);
                generator.putStatic(Type.getType(typeName), name, Type.getType(Descriptor.getFieldDescriptor(type)));
            }

            @Override
            public void visitIncrement(String name, int amount) {
                int id = scope.getVarId(name);
                generator.iinc(id, amount);
            }

            @Override
            public void visitReturnValue(ExpressionDom expression) {
                String resultType = populateMethodExpression(methodNode, originalInstructions, generator, expression, null, true, scope);

                if (resultType.equals(Descriptor.VOID))
                    throw new IllegalArgumentException("Expression of return statement results in void.");

                generator.returnValue();
            }

            @Override
            public void visitBlock(List<StatementDom> statements) {
                statements.forEach(s ->
                    populateMethodStatement(methodNode, originalInstructions, generator, s, breakLabel, labelScope, methodBodyInjection, scope));
            }

            @Override
            public void visitIfElse(ExpressionDom condition, StatementDom ifTrue, StatementDom ifFalse) {
                Label endLabel = generator.newLabel();
                Label ifFalseLabel = generator.newLabel();

                String resultType = populateMethodExpression(methodNode, originalInstructions, generator, condition, ifFalseLabel, false, scope);
                populateMethodStatement(methodNode, originalInstructions, generator, ifTrue, breakLabel, labelScope, methodBodyInjection, scope);
                generator.goTo(endLabel);
                generator.visitLabel(ifFalseLabel);
                populateMethodStatement(methodNode, originalInstructions, generator, ifFalse, breakLabel, labelScope, methodBodyInjection, scope);
                generator.visitLabel(endLabel);
            }

            @Override
            public void visitBreakCase() {
                generator.goTo(breakLabel);
            }

            @Override
            public void visitReturn() {
                generator.visitInsn(Opcodes.RETURN);
            }

            @Override
            public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {
                populateMethodInvocation(methodNode, originalInstructions, generator, scope, invocation, target, type, name, descriptor, arguments, CODE_LEVEL_STATEMENT);
            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
                populateMethodNewInstance(methodNode, originalInstructions, generator, scope, type, parameterTypes, arguments, CODE_LEVEL_STATEMENT);
            }

            @Override
            public void visitLabel(String name) {
                labelScope.label(generator, name);
            }

            @Override
            public void visitGoTo(String name) {
                labelScope.goTo(generator, name);
            }

            @Override
            public void visitSwitch(ExpressionDom expression, Map<Integer, StatementDom> cases, StatementDom defaultBody) {
                populateMethodExpression(methodNode, originalInstructions, generator, expression, null, true, scope);

                Map<Integer, StatementDom> keyToBodyMap = cases;
                int[] keys = keyToBodyMap.keySet().stream().mapToInt(x -> (int) x).toArray();

                generator.tableSwitch(keys, new TableSwitchGenerator() {
                    Label switchEnd;

                    @Override
                    public void generateCase(int key, Label end) {
                        switchEnd = end;

                        StatementDom body = keyToBodyMap.get(key);
                        populateMethodStatement(methodNode, originalInstructions, generator, body, end, labelScope, methodBodyInjection, scope);
                    }

                    @Override
                    public void generateDefault() {
                        populateMethodStatement(methodNode, originalInstructions, generator, defaultBody, switchEnd, labelScope, methodBodyInjection, scope);
                    }
                });
            }

            @Override
            public void visitASM(MethodNode methodNode) {

            }

            @Override
            public void visitMethodBody() {
                //methodNode.instructions.add(originalInstructions);

                Label returnLabel = new Label();

                ListIterator it = originalInstructions.iterator();

                /*
                // Replaces returns with variable administration
                originalInstructions.accept(new InstructionAdapter(generator) {
                    Label returnLabel;
                    int returnVar = -1;

                    @Override
                    public void areturn(Type type) {
                        if(returnVar == -1) {
                            returnLabel = generator.newLabel();
                            returnVar = generator.newLocal(type);
                            generator.storeLocal(returnVar);
                            generator.visitJumpInsn(Opcodes.GOTO, returnLabel);
                        } else
                            super.areturn(type);
                    }

                    @Override
                    public void visitEnd() {
                        if(returnVar != -1) {
                            generator.visitLabel(returnLabel);
                            generator.loadLocal(returnVar);
                            generator.returnValue();
                        }

                        super.visitEnd();
                    }
                });
                */

                // Strip away frames and store return value in a special local variable
                // After strip away frames and beforevstore return value in a special local variable
                // try with try catch in void method
                boolean withReturn = false;
                while(it.hasNext()) {
                    AbstractInsnNode insn = (AbstractInsnNode)it.next();

                    if(insn.getOpcode() == Opcodes.IRETURN
                        || insn.getOpcode() == Opcodes.RETURN
                        || insn.getOpcode() == Opcodes.ARETURN
                        || insn.getOpcode() == Opcodes.LRETURN
                        || insn.getOpcode() == Opcodes.DRETURN) {
                        generator.visitJumpInsn(Opcodes.GOTO, returnLabel);
                        if(!withReturn) {
                            withReturn = true;
                            methodBodyInjection.withReturn++;
                        }
                    } else if(insn.getOpcode() == Opcodes.F_NEW
                        || insn.getOpcode() == Opcodes.F_FULL
                        || insn.getOpcode() == Opcodes.F_APPEND
                        || insn.getOpcode() == Opcodes.F_CHOP
                        || insn.getOpcode() == Opcodes.F_SAME
                        || insn.getOpcode() == Opcodes.F_SAME1) {
                        // Do nothing?
                        insn.toString();
                        insn.accept(generator);

                        insn.accept(new MethodVisitor(Opcodes.ASM5) {
                            @Override
                            public void visitFrame(int i, int i1, Object[] objects, int i2, Object[] objects1) {
                                super.visitFrame(i, i1, objects, i2, objects1);
                            }

                            @Override
                            public void visitLabel(Label label) {
                                super.visitLabel(label);
                            }
                        });
                    } else {

                        insn.accept(generator);
                    }
                }

                methodBodyInjection.occurrences++;

                generator.visitLabel(returnLabel);
            }

            @Override
            public void visitThrow(ExpressionDom expression) {
                populateMethodExpression(methodNode, originalInstructions, generator, expression, null, true, scope);
                generator.throwException();
            }

            @Override
            public void visitTryCatch(StatementDom tryBlock, List<CodeDom> catchBlocks) {
                Optional<CodeDom> finallyBlock = catchBlocks.stream().filter(cb -> Util.returnFrom(false, r -> cb.accept(new DefaultCodeDomVisitor() {
                    @Override
                    public void visitCatch(String type, String name, StatementDom statementDom) {
                        if(type == null)
                            r.accept(true);
                    }
                }))).findFirst();

                Label tryStart = generator.newLabel();
                Label tryEnd = generator.newLabel();
                Label endAll = generator.newLabel();
                InstructionAdapter instructionAdapter =
                    finallyBlock.isPresent() ? new ReplaceReturnWithStore(generator)
                    : new InstructionAdapter(generator);

                Method m = new Method(methodNode.name, methodNode.desc);
                GeneratorAdapter innerGenerator = new GeneratorAdapter(methodNode.access, m, instructionAdapter);

                generator.visitLabel(tryStart);
                populateMethodStatement(methodNode, originalInstructions, innerGenerator, tryBlock, breakLabel, labelScope, methodBodyInjection, scope);
                generator.visitLabel(tryEnd);

                if(finallyBlock.isPresent()) {
                    StatementDom statementDom = Util.returnFrom(null, r -> finallyBlock.get().accept(new DefaultCodeDomVisitor() {
                        @Override
                        public void visitCatch(String type, String name, StatementDom statementDom) {
                            r.accept(statementDom);
                        }
                    }));

                    GenerateScope finallyScope = new GenerateScope(scope);

                    ((ReplaceReturnWithStore)instructionAdapter).returnStart();
                    populateMethodStatement(methodNode, originalInstructions, generator, statementDom, breakLabel, labelScope, methodBodyInjection, finallyScope);
                    ((ReplaceReturnWithStore)instructionAdapter).returnEnd();
                }

                generator.visitJumpInsn(Opcodes.GOTO, endAll);

                ArrayList<Pair<Label, Label>> attempts = new ArrayList<Pair<Label, Label>>();

                attempts.add(new Pair<>(tryStart, tryEnd));

                catchBlocks.forEach(cb -> {
                    cb.accept(new DefaultCodeDomVisitor() {
                        @Override
                        public void visitCatch(String type, String name, StatementDom statementDom) {
                            if (type != null) {
                                // A non-finally catch
                                Label handlerStart = generator.newLabel();
                                Label handlerEnd = generator.newLabel();

                                GenerateScope catchScope = new GenerateScope(scope);

                                catchScope.declareVar(generator, type, name);

                                InstructionAdapter instructionAdapter = finallyBlock.isPresent()
                                    ? new ReplaceReturnWithStore(generator)
                                    : new InstructionAdapter(generator);

                                Method m = new Method(methodNode.name, methodNode.desc);
                                GeneratorAdapter innerGenerator = new GeneratorAdapter(methodNode.access, m, instructionAdapter);

                                generator.visitLabel(handlerStart);
                                generator.storeLocal(catchScope.getVarId(name));
                                populateMethodStatement(methodNode, originalInstructions, innerGenerator, statementDom, breakLabel, labelScope, methodBodyInjection, catchScope);
                                generator.visitLabel(handlerEnd);

                                generator.visitTryCatchBlock(tryStart, tryEnd, handlerStart, type);

                                attempts.add(new Pair<>(handlerStart, handlerEnd));

                                if (finallyBlock.isPresent()) {
                                    StatementDom finallyStatementDom = Util.returnFrom(null, r -> finallyBlock.get().accept(new DefaultCodeDomVisitor() {
                                        @Override
                                        public void visitCatch(String type, String name, StatementDom statementDom) {
                                            r.accept(statementDom);
                                        }
                                    }));

                                    GenerateScope finallyScope = new GenerateScope(scope);

                                    ((ReplaceReturnWithStore)instructionAdapter).returnStart();
                                    populateMethodStatement(methodNode, originalInstructions, generator, finallyStatementDom, breakLabel, labelScope, methodBodyInjection, finallyScope);
                                    ((ReplaceReturnWithStore)instructionAdapter).returnEnd();
                                }

                                generator.visitJumpInsn(Opcodes.GOTO, endAll);
                            }
                        }
                    });
                });

                if (finallyBlock.isPresent()) {
                    // Something goes wrong in try block or a catch block
                    attempts.forEach(x -> {
                        StatementDom finallyStatementDom = Util.returnFrom(null, r -> finallyBlock.get().accept(new DefaultCodeDomVisitor() {
                            @Override
                            public void visitCatch(String type, String name, StatementDom statementDom) {
                                r.accept(statementDom);
                            }
                        }));

                        Label finallyHandlerStart = generator.newLabel();

                        GenerateScope finallyScope = new GenerateScope(scope);

                        int finallyExceptionId = generator.newLocal(Type.getType(Exception.class));

                        generator.visitLabel(finallyHandlerStart);
                        generator.storeLocal(finallyExceptionId);
                        populateMethodStatement(methodNode, originalInstructions, generator, finallyStatementDom, breakLabel, labelScope, methodBodyInjection, finallyScope);
                        generator.loadLocal(finallyExceptionId);
                        generator.throwException();

                        generator.visitTryCatchBlock(x.getKey(), x.getValue(), finallyHandlerStart, null);
                    });
                }

                generator.visitLabel(endAll);
            }
        });

        return Descriptor.VOID;
    }

    public String populateMethodExpression(MethodNode methodNode, InsnList originalInstructions, GeneratorAdapter generator, ExpressionDom expression, Label ifFalseLabel, boolean reifyCondition, GenerateScope scope) {
        return new ExpressionDomVisitor.Return<String>() {
            @Override
            public void visitBooleanLiteral(boolean value) {
                if(!value) {
                    if(reifyCondition)
                        generator.push(value);
                    if(ifFalseLabel != null)
                        generator.goTo(ifFalseLabel);
                } else {
                    if(reifyCondition)
                        generator.push(value);
                }

                setResult(Descriptor.BOOLEAN);
            }

            @Override
            public void visitByteLiteral(byte value) {
                generator.push(value);

                setResult(Descriptor.BYTE);
            }

            @Override
            public void visitShortLiteral(short value) {
                generator.push(value);

                setResult(Descriptor.SHORT);
            }

            @Override
            public void visitIntLiteral(int value) {
                generator.push(value);

                setResult(Descriptor.INT);
            }

            @Override
            public void visitLongLiteral(long value) {
                generator.push(value);

                setResult(Descriptor.LONG);
            }

            @Override
            public void visitFloatLiteral(float value) {
                generator.push(value);

                setResult(Descriptor.FLOAT);
            }

            @Override
            public void visitDoubleLiteral(double value) {
                generator.push(value);

                setResult(Descriptor.DOUBLE);
            }

            @Override
            public void visitCharLiteral(char value) {
                generator.push(value);

                setResult(Descriptor.CHAR);
            }

            @Override
            public void visitStringLiteral(String value) {
                generator.push(value);

                setResult(Descriptor.STRING);
            }

            @Override
            public void visitNull() {
                generator.visitInsn(Opcodes.ACONST_NULL);

                // Cannot determine type? // Object is the most specific type?
                setResult(Descriptor.get(Object.class));
            }

            @Override
            public void visitArithmetic(int operator, ExpressionDom lhs, ExpressionDom rhs) {
                int op;

                switch(operator) {
                    case ArithmeticOperator.ADD: op = GeneratorAdapter.ADD; break;
                    case ArithmeticOperator.SUB: op = GeneratorAdapter.SUB; break;
                    case ArithmeticOperator.MUL: op = GeneratorAdapter.MUL; break;
                    case ArithmeticOperator.DIV: op = GeneratorAdapter.DIV; break;
                    case ArithmeticOperator.REM: op = GeneratorAdapter.REM; break;
                    default: op = -1;
                }

                String lhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, lhs, ifFalseLabel, false, scope);
                String rhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, rhs, ifFalseLabel, reifyCondition, scope);

                String resultType = arithmeticResultType(lhsResultType, rhsResultType);
                Type t = Type.getType(resultType);
                generator.math(op, t);

                setResult(resultType);
            }

            @Override
            public void visitShift(int operator, ExpressionDom lhs, ExpressionDom rhs) {
                int op;

                switch(operator) {
                    case ShiftOperator.SHL: op = GeneratorAdapter.SHL; break;
                    case ShiftOperator.SHR: op = GeneratorAdapter.SHR; break;
                    case ShiftOperator.USHR: op = GeneratorAdapter.USHR; break;
                    default: op = -1;
                }

                String lhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, lhs, ifFalseLabel, reifyCondition, scope);
                String rhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, rhs, ifFalseLabel, reifyCondition, scope);
                String resultType = shiftResultType(lhsResultType, rhsResultType);
                Type t = Type.getType(resultType);
                generator.math(op, t);

                setResult(resultType);
            }

            @Override
            public void visitBitwise(int operator, ExpressionDom lhs, ExpressionDom rhs) {
                int op;

                switch(operator) {
                    case BitwiseOperator.AND: op = GeneratorAdapter.AND; break;
                    case BitwiseOperator.OR: op = GeneratorAdapter.OR; break;
                    case BitwiseOperator.XOR: op = GeneratorAdapter.XOR; break;
                    default: op = -1;
                }

                String lhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, lhs, ifFalseLabel, reifyCondition, scope);
                String rhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, rhs, ifFalseLabel, reifyCondition, scope);
                String resultType = bitwiseResultType(lhsResultType, rhsResultType);
                Type t = Type.getType(resultType);
                generator.math(op, t);

                setResult(resultType);
            }

            @Override
            public void visitCompare(int operator, ExpressionDom lhs, ExpressionDom rhs) {
                int op;

                switch (operator) {
                    case RelationalOperator.LT: op = GeneratorAdapter.GE; break;
                    case RelationalOperator.LE: op = GeneratorAdapter.GT; break;
                    case RelationalOperator.GT: op = GeneratorAdapter.LE; break;
                    case RelationalOperator.GE: op = GeneratorAdapter.LT; break;
                    case RelationalOperator.EQ: op = GeneratorAdapter.NE; break;
                    case RelationalOperator.NE: op = GeneratorAdapter.EQ; break;
                    default: op = -1;
                }

                String lhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, lhs, ifFalseLabel, reifyCondition, scope);
                String rhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, rhs, ifFalseLabel, reifyCondition, scope);

                Type t = Type.getType(lhsResultType);

                if (reifyCondition) {
                    Label endLabel = generator.newLabel();
                    Label innerIfFalseLabel = generator.newLabel();

                    generator.ifCmp(t, op, innerIfFalseLabel);
                    generator.push(true);
                    generator.goTo(endLabel);
                    generator.visitLabel(innerIfFalseLabel);
                    generator.push(false);
                    generator.visitLabel(endLabel);
                } else {
                    generator.ifCmp(t, op, ifFalseLabel);
                }

                setResult(Descriptor.BOOLEAN);
            }

            @Override
            public void visitLogical(int operator, ExpressionDom lhs, ExpressionDom rhs) {
                String resultType = null;

                switch(operator) {
                    case LogicalOperator.AND: {
                        Label lhsIfFalseLabel = ifFalseLabel != null ? ifFalseLabel : generator.newLabel();
                        boolean lhsReify = ifFalseLabel != null ? false : true;
                        String lhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, lhs, lhsIfFalseLabel, lhsReify, scope);
                        String rhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, rhs, ifFalseLabel, reifyCondition, scope);

                        if(ifFalseLabel == null) {
                            generator.visitLabel(lhsIfFalseLabel);
                        }

                        resultType = logicalResultType(lhsResultType, rhsResultType);

                        break;
                    }
                    case LogicalOperator.OR: {
                        Label endLabel = generator.newLabel();
                        Label nextTestLabel = generator.newLabel();
                        String lhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, lhs, nextTestLabel, false, scope);
                        if(reifyCondition)
                            generator.push(true);
                        generator.goTo(endLabel);
                        generator.visitLabel(nextTestLabel);
                        String rhsResultType = populateMethodExpression(methodNode, originalInstructions, generator, rhs, ifFalseLabel, reifyCondition, scope);
                        generator.visitLabel(endLabel);
                        resultType = logicalResultType(lhsResultType, rhsResultType);

                        break;
                    }
                }

                setResult(resultType);
            }

            @Override
            public void visitVariableAccess(String name) {
                OptionalInt parameterOrdinal = IntStream.range(0, parameters.size()).filter(x -> parameters.get(x).name.equals(name)).findFirst();

                if(parameterOrdinal.isPresent()) {
                    generator.loadArg(parameterOrdinal.getAsInt());
                    setResult(parameters.get(parameterOrdinal.getAsInt()).descriptor);
                } else {
                    int id = scope.getVarId(name);
                    generator.loadLocal(id);

                    setResult(scope.getVarType(name));
                }
            }

            @Override
            public void visitFieldAccess(ExpressionDom target, String name, String fieldTypeName) {
                String targetType = populateMethodExpression(methodNode, originalInstructions, generator, target, null, true, scope);
                generator.getField(Type.getType(targetType), name, Type.getType(Descriptor.getFieldDescriptor(fieldTypeName)));

                setResult(fieldTypeName);
            }

            @Override
            public void visitStaticFieldAccess(String typeName, String name, String fieldTypeName) {
                generator.getStatic(Type.getType(typeName), name, Type.getType(Descriptor.getFieldDescriptor(fieldTypeName)));

                setResult(fieldTypeName);
            }

            @Override
            public void visitNot(ExpressionDom expression) {
                String resultType = populateMethodExpression(methodNode, originalInstructions, generator, expression, null, true, scope);

                if(reifyCondition)
                    generator.not();
                if(ifFalseLabel != null)
                    generator.ifZCmp(GeneratorAdapter.NE, ifFalseLabel);

                setResult(Descriptor.BOOLEAN);
            }

            @Override
            public void visitInstanceOf(ExpressionDom expression, String type) {
                String resultType = populateMethodExpression(methodNode, originalInstructions, generator, expression, null, true, scope);
                Type t = Type.getType(type);

                generator.instanceOf(t);

                setResult(Descriptor.BOOLEAN);
            }

            @Override
            public void visitBlock(List<CodeDom> codeList) {
                // Exactly one expression should be contained within codeList
                List<String> expressionResultTypes = new ArrayList<>();

                LabelScope labelScope = new LabelScope();
                MethodBodyInjection returnLabel = new MethodBodyInjection();

                codeList.forEach(code -> {
                    code.accept(new DefaultCodeDomVisitor() {
                        @Override
                        public void visitStatement(StatementDom statementDom) {
                            populateMethodStatement(methodNode, originalInstructions, generator, statementDom, null, labelScope, returnLabel, scope);
                        }

                        @Override
                        public void visitExpression(ExpressionDom expressionDom) {
                            String resultType = populateMethodExpression(methodNode, originalInstructions, generator, expressionDom, null, true, scope);
                            expressionResultTypes.add(resultType);
                        }
                    });
                });

                int expressionCount = 0;

                expressionCount += expressionResultTypes.size();
                expressionCount += returnLabel.withReturn; // Method body with a return counts as an expression

                labelScope.verify();

                if(expressionCount > 1)
                    throw new IllegalArgumentException("Expression block has multiple expressions.");
                else if(expressionCount == 0)
                    throw new IllegalArgumentException("Expression block has no expressions.");

                setResult(expressionResultTypes.get(0));
            }

            @Override
            public void visitIfElse(ExpressionDom condition, ExpressionDom ifTrue, ExpressionDom ifFalse) {
                Label endLabel = generator.newLabel();
                Label testIfFalseLabel = generator.newLabel();

                String resultType = populateMethodExpression(methodNode, originalInstructions, generator, condition, testIfFalseLabel, false, scope);
                String ifTrueResultType = populateMethodExpression(methodNode, originalInstructions, generator, ifTrue, null, true, scope);
                generator.goTo(endLabel);
                generator.visitLabel(testIfFalseLabel);
                String ifFalseResultType = populateMethodExpression(methodNode, originalInstructions, generator, ifFalse, null, true, scope);
                generator.visitLabel(endLabel);

                if(!ifTrueResultType.equals(ifFalseResultType))
                    throw new IllegalArgumentException("Inconsistent result types in test: ifTrue => " + ifTrueResultType + ", ifFalse => " + ifFalseResultType);

                setResult(ifTrueResultType);
            }

            @Override
            public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {
                String resultType = populateMethodInvocation(methodNode, originalInstructions, generator, scope, invocation, target, type, name, descriptor, arguments, CODE_LEVEL_EXPRESSION);
                setResult(resultType);
            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
                String resultType = populateMethodNewInstance(methodNode, originalInstructions, generator, scope, type, parameterTypes, arguments, CODE_LEVEL_EXPRESSION);
                setResult(resultType);
            }

            @Override
            public void visitThis() {
                generator.loadThis();
                setResult(Descriptor.get(thisClassName));
            }

            @Override
            public void visitTop(ExpressionDom expression, BiFunction<ExpressionDom, ExpressionDom, ExpressionDom> usage) {
                String topResultType = populateMethodExpression(methodNode, originalInstructions, generator, expression, ifFalseLabel, reifyCondition, scope);
                ExpressionDom dup = v -> v.visitDup(topResultType);
                ExpressionDom last = v -> v.visitLetBe(topResultType);
                ExpressionDom usageExpression = usage.apply(dup, last);
                String resultType = populateMethodExpression(methodNode, originalInstructions, generator, usageExpression, ifFalseLabel, reifyCondition, scope);
                setResult(resultType);
            }

            @Override
            public void visitDup(String type) {
                generator.dup();
                setResult(type);
            }

            @Override
            public void visitLetBe(String type) {
                setResult(type);
            }

            @Override
            public void visitTypeCast(ExpressionDom expression, String targetType) {
                String resultType = populateMethodExpression(methodNode, originalInstructions, generator, expression, null, true, scope);
                //generator.cast(Type.getType(resultType), Type.getType(targetType));
                generator.checkCast(Type.getType(targetType));
                setResult(targetType);
            }
        }.returnFrom(expression);
    }

    private static final int CODE_LEVEL_STATEMENT = 0;
    private static final int CODE_LEVEL_EXPRESSION = 1;

    private String populateMethodInvocation(
        MethodNode methodNode, InsnList originalInstructions, GeneratorAdapter generator, GenerateScope scope,
        int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments,
        int codeLevel) {
        String returnType = descriptor.substring(descriptor.indexOf(")") + 1);

        if(codeLevel == CODE_LEVEL_EXPRESSION && returnType.equals(Descriptor.VOID))
            throw new IllegalArgumentException("Invocations at expression level must return non-void occurrences.");

        // Push target for instance invocations
        if(target != null)
            populateMethodExpression(methodNode, originalInstructions, generator, target, null, true, scope);

        arguments.forEach(a ->
            populateMethodExpression(methodNode, originalInstructions, generator, a, null, true, scope));

        switch (invocation) {
            case Invocation.INTERFACE:
                generator.invokeInterface(Type.getType(type), new Method(name, descriptor));
                break;
            case Invocation.STATIC:
                generator.invokeStatic(Type.getType(type), new Method(name, descriptor));
                break;
            case Invocation.VIRTUAL:
                generator.invokeVirtual(Type.getType(type), new Method(name, descriptor));
                break;
            case Invocation.SPECIAL:
                generator.invokeConstructor(Type.getType(type), new Method(name, descriptor));
                break;
        }

        if(codeLevel == CODE_LEVEL_STATEMENT && !returnType.equals(Descriptor.VOID))
            generator.pop(); // Pop unused return occurrences

        return returnType;
    }

    private String populateMethodNewInstance(
        MethodNode methodNode, InsnList originalInstructions, GeneratorAdapter generator, GenerateScope scope,
        String type, List<String> parameterTypes, List<ExpressionDom> arguments,
        int codeLevel) {
        String returnType = type;

        generator.newInstance(Type.getType(type));
        generator.dup();
        arguments.forEach(a ->
            populateMethodExpression(methodNode, originalInstructions, generator, a, null, true, scope));
        generator.invokeConstructor(Type.getType(type), new Method("<init>", Descriptor.getMethodDescriptor(parameterTypes, Descriptor.VOID)));

        if(codeLevel == CODE_LEVEL_STATEMENT) {
            generator.pop();
            returnType = Descriptor.VOID;
        }

        return returnType;
    }
}

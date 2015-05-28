package astava.java.gen;

import astava.java.*;
import astava.tree.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.TableSwitchGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static astava.java.Factory.*;

public class MethodGenerator {
    private StatementDom body;
    private GenerateScope methodScope;

    public MethodGenerator(StatementDom body) {
        this.body = body;
        this.methodScope = new GenerateScope();
    }

    public void generate(GeneratorAdapter generator) {
        LabelScope labelScope = new LabelScope();
        generator.visitCode();
        populateMethodStatement(generator, body, null, labelScope);
        generator.visitEnd();
        generator.visitMaxs(0, 0);
        labelScope.verify();
    }

    public String populateMethodStatement(GeneratorAdapter generator, StatementDom statement, Label breakLabel, LabelScope labelScope) {
        statement.accept(new StatementDomVisitor() {
            @Override
            public void visitVariableDeclaration(String type, String name) {
                methodScope.declareVar(generator, type, name);
            }

            @Override
            public void visitVariableAssignment(String name, ExpressionDom value) {
                String valueType = populateMethodExpression(generator, value, null, true);
                int id = methodScope.getVarId(name);
                generator.storeLocal(id, Type.getType(valueType));
            }

            @Override
            public void visitIncrement(String name, int amount) {
                int id = methodScope.getVarId(name);
                generator.iinc(id, amount);
            }

            @Override
            public void visitReturnValue(ExpressionDom expression) {
                String resultType = populateMethodExpression(generator, expression, null, true);

                if (resultType.equals(Descriptor.VOID))
                    throw new IllegalArgumentException("Expression of return statement results in void.");

                generator.returnValue();
            }

            @Override
            public void visitBlock(List<StatementDom> statements) {
                statements.forEach(s ->
                    populateMethodStatement(generator, s, breakLabel, labelScope));
            }

            @Override
            public void visitIfElse(ExpressionDom condition, StatementDom ifTrue, StatementDom ifFalse) {
                Label endLabel = generator.newLabel();
                Label ifFalseLabel = generator.newLabel();

                String resultType = populateMethodExpression(generator, condition, ifFalseLabel, false);
                populateMethodStatement(generator, ifTrue, breakLabel, labelScope);
                generator.goTo(endLabel);
                generator.visitLabel(ifFalseLabel);
                populateMethodStatement(generator, ifFalse, breakLabel, labelScope);
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
                populateMethodInvocation(generator, methodScope, invocation, target, type, name, descriptor, arguments, CODE_LEVEL_STATEMENT);
            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
                populateMethodNewInstance(generator, methodScope, type, parameterTypes, arguments, CODE_LEVEL_STATEMENT);
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
                populateMethodExpression(generator, expression, null, true);

                Map<Integer, StatementDom> keyToBodyMap = cases;
                int[] keys = keyToBodyMap.keySet().stream().mapToInt(x -> (int) x).toArray();

                generator.tableSwitch(keys, new TableSwitchGenerator() {
                    Label switchEnd;

                    @Override
                    public void generateCase(int key, Label end) {
                        switchEnd = end;

                        StatementDom body = keyToBodyMap.get(key);
                        populateMethodStatement(generator, body, end, labelScope);
                    }

                    @Override
                    public void generateDefault() {
                        populateMethodStatement(generator, defaultBody, switchEnd, labelScope);
                    }
                });
            }
        });

        return Descriptor.VOID;
    }

    public String populateMethodExpression(GeneratorAdapter generator, ExpressionDom expression, Label ifFalseLabel, boolean reifyCondition) {
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

                String lhsResultType = populateMethodExpression(generator, lhs, ifFalseLabel, false);
                String rhsResultType = populateMethodExpression(generator, rhs, ifFalseLabel, reifyCondition);

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

                String lhsResultType = populateMethodExpression(generator, lhs, ifFalseLabel, reifyCondition);
                String rhsResultType = populateMethodExpression(generator, rhs, ifFalseLabel, reifyCondition);
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

                String lhsResultType = populateMethodExpression(generator, lhs, ifFalseLabel, reifyCondition);
                String rhsResultType = populateMethodExpression(generator, rhs, ifFalseLabel, reifyCondition);
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

                String lhsResultType = populateMethodExpression(generator, lhs, ifFalseLabel, reifyCondition);
                String rhsResultType = populateMethodExpression(generator, rhs, ifFalseLabel, reifyCondition);

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
                        String lhsResultType = populateMethodExpression(generator, lhs, lhsIfFalseLabel, lhsReify);
                        String rhsResultType = populateMethodExpression(generator, rhs, ifFalseLabel, reifyCondition);

                        if(ifFalseLabel == null) {
                            generator.visitLabel(lhsIfFalseLabel);
                        }

                        resultType = logicalResultType(lhsResultType, rhsResultType);

                        break;
                    }
                    case LogicalOperator.OR: {
                        Label endLabel = generator.newLabel();
                        Label nextTestLabel = generator.newLabel();
                        String lhsResultType = populateMethodExpression(generator, lhs, nextTestLabel, false);
                        if(reifyCondition)
                            generator.push(true);
                        generator.goTo(endLabel);
                        generator.visitLabel(nextTestLabel);
                        String rhsResultType = populateMethodExpression(generator, rhs, ifFalseLabel, reifyCondition);
                        generator.visitLabel(endLabel);
                        resultType = logicalResultType(lhsResultType, rhsResultType);

                        break;
                    }
                }

                setResult(resultType);
            }

            @Override
            public void visitVariableAccess(String name) {
                int id = methodScope.getVarId(name);
                generator.loadLocal(id);

                setResult(methodScope.getVarType(name));
            }

            @Override
            public void visitNot(ExpressionDom expression) {
                String resultType = populateMethodExpression(generator, expression, null, true);

                if(reifyCondition)
                    generator.not();
                if(ifFalseLabel != null)
                    generator.ifZCmp(GeneratorAdapter.NE, ifFalseLabel);

                setResult(Descriptor.BOOLEAN);
            }

            @Override
            public void visitInstanceOf(ExpressionDom expression, String type) {
                String resultType = populateMethodExpression(generator, expression, null, true);
                Type t = Type.getType(type);

                generator.instanceOf(t);

                setResult(Descriptor.BOOLEAN);
            }

            @Override
            public void visitBlock(List<CodeDom> codeList) {
                // Exactly one expression should be contained within codeList
                List<String> expressionResultTypes = new ArrayList<>();

                LabelScope labelScope = new LabelScope();

                codeList.forEach(code -> {
                    code.accept(new CodeDomVisitor() {
                        @Override
                        public void visitStatement(StatementDom statementDom) {
                            populateMethodStatement(generator, statementDom, null, labelScope);
                        }

                        @Override
                        public void visitExpression(ExpressionDom expressionDom) {
                            String resultType = populateMethodExpression(generator, expressionDom, null, true);
                            expressionResultTypes.add(resultType);
                        }
                    });
                });

                labelScope.verify();

                if(expressionResultTypes.size() > 1)
                    throw new IllegalArgumentException("Expression block has multiple expressions.");
                else if(expressionResultTypes.isEmpty())
                    throw new IllegalArgumentException("Expression block has no expressions.");

                setResult(expressionResultTypes.get(0));
            }

            @Override
            public void visitIfElse(ExpressionDom condition, ExpressionDom ifTrue, ExpressionDom ifFalse) {
                Label endLabel = generator.newLabel();
                Label testIfFalseLabel = generator.newLabel();

                String resultType = populateMethodExpression(generator, condition, testIfFalseLabel, false);
                String ifTrueResultType = populateMethodExpression(generator, ifTrue, null, true);
                generator.goTo(endLabel);
                generator.visitLabel(testIfFalseLabel);
                String ifFalseResultType = populateMethodExpression(generator, ifFalse, null, true);
                generator.visitLabel(endLabel);

                if(!ifTrueResultType.equals(ifFalseResultType))
                    throw new IllegalArgumentException("Inconsistent result types in test: ifTrue => " + ifTrueResultType + ", ifFalse => " + ifFalseResultType);

                setResult(ifTrueResultType);
            }

            @Override
            public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {
                String resultType = populateMethodInvocation(generator, methodScope, invocation, target, type, name, descriptor, arguments, CODE_LEVEL_EXPRESSION);
                setResult(resultType);
            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
                String resultType = populateMethodNewInstance(generator, methodScope, type, parameterTypes, arguments, CODE_LEVEL_EXPRESSION);
                setResult(resultType);
            }
        }.returnFrom(expression);
    }

    private static final int CODE_LEVEL_STATEMENT = 0;
    private static final int CODE_LEVEL_EXPRESSION = 1;

    private String populateMethodInvocation(
        GeneratorAdapter generator, GenerateScope methodScope,
        int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments,
        int codeLevel) {
        String returnType = descriptor.substring(descriptor.indexOf(")") + 1);

        if(codeLevel == CODE_LEVEL_EXPRESSION && returnType.equals(Descriptor.VOID))
            throw new IllegalArgumentException("Invocations at expression level must return non-void value.");

        // Push target for instance invocations
        if(target != null)
            populateMethodExpression(generator, target, null, true);

        arguments.forEach(a ->
            populateMethodExpression(generator, a, null, true));

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
        }

        if(codeLevel == CODE_LEVEL_STATEMENT && !returnType.equals(Descriptor.VOID))
            generator.pop(); // Pop unused return value

        return returnType;
    }

    private String populateMethodNewInstance(
        GeneratorAdapter generator, GenerateScope methodScope,
        String type, List<String> parameterTypes, List<ExpressionDom> arguments,
        int codeLevel) {
        String returnType = type;

        generator.newInstance(Type.getType(type));
        generator.dup();
        arguments.forEach(a ->
            populateMethodExpression(generator, a, null, true));
        generator.invokeConstructor(Type.getType(type), new Method("<init>", Descriptor.getMethodDescriptor(parameterTypes, Descriptor.VOID)));

        if(codeLevel == CODE_LEVEL_STATEMENT) {
            generator.pop();
            returnType = Descriptor.VOID;
        }

        return returnType;
    }
}
package astava.java.gen;

import astava.core.Node;
import astava.core.Tuple;
import astava.java.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.TableSwitchGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodGenerator {
    private Tuple body;
    private GenerateScope methodScope;

    public MethodGenerator(Tuple body) {
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

    public String populateMethodStatement(GeneratorAdapter generator, Tuple statement, Label breakLabel, LabelScope labelScope) {
        switch(statement.getIntProperty(Property.KEY_AST_TYPE)) {
            case ASTType.VARIABLE_DECLARATION: {
                String type = statement.getStringProperty(Property.KEY_VAR_TYPE);
                String name = statement.getStringProperty(Property.KEY_NAME);
                methodScope.declareVar(generator, type, name);

                break;
            } case ASTType.VARIABLE_ASSIGNMENT: {
                String name = statement.getStringProperty(Property.KEY_NAME);
                Tuple value = statement.getTupleProperty(Property.KEY_EXPRESSION);
                String valueType = populateMethodExpression(generator, value, null, true);
                int id = methodScope.getVarId(name);
                generator.storeLocal(id, Type.getType(valueType));

                break;
            } case ASTType.INCREMENT: {
                String name = statement.getStringProperty(Property.KEY_NAME);
                int amount = statement.getIntProperty(Property.KEY_AMOUNT);
                int id = methodScope.getVarId(name);
                generator.iinc(id, amount);

                break;
            } case ASTType.RETURN_VALUE_STATEMENT: {
                Tuple expression = statement.getTupleProperty(Property.KEY_EXPRESSION);

                String resultType = populateMethodExpression(generator, expression, null, true);

                if(resultType.equals(Descriptor.VOID))
                    throw new IllegalArgumentException("Expression of return statement results in void.");

                generator.returnValue();

                break;
            } case ASTType.BLOCK: {
                List<Node> statements = (List<Node>) statement.getPropertyValueAs(Property.KEY_STATEMENTS, List.class);

                statements.forEach(s ->
                    populateMethodStatement(generator, (Tuple) s, breakLabel, labelScope));

                break;
            } case ASTType.IF_ELSE: {
                Tuple condition = statement.getTupleProperty(Property.KEY_CONDITION);
                Tuple ifTrue = statement.getTupleProperty(Property.KEY_IF_TRUE);
                Tuple ifFalse = statement.getTupleProperty(Property.KEY_IF_FALSE);

                Label endLabel = generator.newLabel();
                Label ifFalseLabel = generator.newLabel();

                String resultType = populateMethodExpression(generator, condition, ifFalseLabel, false);
                populateMethodStatement(generator, ifTrue, breakLabel, labelScope);
                generator.goTo(endLabel);
                generator.visitLabel(ifFalseLabel);
                populateMethodStatement(generator, ifFalse, breakLabel, labelScope);
                generator.visitLabel(endLabel);

                break;
            } case ASTType.BREAK_CASE: {
                generator.goTo(breakLabel);
                break;
            } case ASTType.RETURN_STATEMENT: {
                generator.visitInsn(Opcodes.RETURN);
                break;
            } case ASTType.INVOCATION: {
                populateMethodInvocation(generator, methodScope, statement, CODE_LEVEL_STATEMENT);
                break;
            } case ASTType.NEW_INSTANCE: {
                populateMethodNewInstance(generator, methodScope, statement, CODE_LEVEL_STATEMENT);
                break;
            } case ASTType.LABEL: {
                String name = statement.getStringProperty(Property.KEY_NAME);

                labelScope.label(generator, name);

                break;
            } case ASTType.GO_TO: {
                String name = statement.getStringProperty(Property.KEY_NAME);

                labelScope.goTo2(generator, name);

                break;
            } case ASTType.SWITCH: {
                Tuple expression = statement.getTupleProperty(Property.KEY_EXPRESSION);
                List<Node> cases = (List<Node>) statement.getPropertyValueAs(Property.KEY_CASES, List.class);
                Tuple defaultBody = statement.getTupleProperty(Property.KEY_DEFAULT);

                populateMethodExpression(generator, expression, null, true);

                Map<Integer, Tuple> keyToBodyMap = cases.stream()
                        .collect(Collectors.toMap(x -> ((Tuple) x).getIntProperty(Property.KEY_KEY), x -> ((Tuple) x).getTupleProperty(Property.KEY_BODY)));
                int[] keys = keyToBodyMap.keySet().stream().mapToInt(x -> (int)x).toArray();

                generator.tableSwitch(keys, new TableSwitchGenerator() {
                    Label switchEnd;

                    @Override
                    public void generateCase(int key, Label end) {
                        switchEnd = end;

                        Tuple body = keyToBodyMap.get(key);
                        populateMethodStatement(generator, body, end, labelScope);
                    }

                    @Override
                    public void generateDefault() {
                        populateMethodStatement(generator, defaultBody, switchEnd, labelScope);
                    }
                });

                break;
            } default: {
                return null; // Not a statement
            }
        }

        return Descriptor.VOID;
    }

    public String populateMethodExpression(GeneratorAdapter generator, Tuple expression, Label ifFalseLabel, boolean reifyCondition) {
        switch(expression.getIntProperty(Property.KEY_AST_TYPE)) {
            case ASTType.BOOLEAN_LITERAL: {
                boolean value = expression.getBooleanProperty(Property.KEY_VALUE);

                if(!value) {
                    if(reifyCondition)
                        generator.push(value);
                    if(ifFalseLabel != null)
                        generator.goTo(ifFalseLabel);
                } else {
                    if(reifyCondition)
                        generator.push(value);
                }

                return Descriptor.BOOLEAN;
            } case ASTType.BYTE_LITERAL: {
                byte value = expression.getByteProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.BYTE;
            } case ASTType.SHORT_LITERAL: {
                short value = expression.getShortProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.SHORT;
            } case ASTType.INT_LITERAL: {
                int value = expression.getIntProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.INT;
            } case ASTType.LONG_LITERAL: {
                long value = expression.getLongProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.LONG;
            } case ASTType.FLOAT_LITERAL: {
                float value = expression.getFloatProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.FLOAT;
            } case ASTType.DOUBLE_LITERAL: {
                double value = expression.getDoubleProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.DOUBLE;
            } case ASTType.CHAR_LITERAL: {
                char value = expression.getCharProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.CHAR;
            } case ASTType.STRING_LITERAL: {
                String value = expression.getStringProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.STRING;
            } case ASTType.REDUCE: {
                Tuple lhs = (Tuple)expression.getPropertyValue(Property.KEY_LHS);
                Tuple rhs = (Tuple)expression.getPropertyValue(Property.KEY_RHS);

                int operator = expression.getIntProperty(Property.KEY_OPERATOR);
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

                String resultType = Factory.arithmeticResultType(lhsResultType, rhsResultType);
                Type t = Type.getType(resultType);
                generator.math(op, t);

                return resultType;
            } case ASTType.SHIFT: {
                Tuple lhs = (Tuple)expression.getPropertyValue(Property.KEY_LHS);
                Tuple rhs = (Tuple)expression.getPropertyValue(Property.KEY_RHS);

                int operator = expression.getIntProperty(Property.KEY_OPERATOR);
                int op;

                switch(operator) {
                    case ShiftOperator.SHL: op = GeneratorAdapter.SHL; break;
                    case ShiftOperator.SHR: op = GeneratorAdapter.SHR; break;
                    case ShiftOperator.USHR: op = GeneratorAdapter.USHR; break;
                    default: op = -1;
                }

                String lhsResultType = populateMethodExpression(generator, lhs, ifFalseLabel, reifyCondition);
                String rhsResultType = populateMethodExpression(generator, rhs, ifFalseLabel, reifyCondition);
                String resultType = Factory.shiftResultType(lhsResultType, rhsResultType);
                Type t = Type.getType(resultType);
                generator.math(op, t);

                return resultType;
            } case ASTType.BITWISE: {
                Tuple lhs = (Tuple)expression.getPropertyValue(Property.KEY_LHS);
                Tuple rhs = (Tuple)expression.getPropertyValue(Property.KEY_RHS);

                int operator = expression.getIntProperty(Property.KEY_OPERATOR);
                int op;

                switch(operator) {
                    case BitwiseOperator.AND: op = GeneratorAdapter.AND; break;
                    case BitwiseOperator.OR: op = GeneratorAdapter.OR; break;
                    case BitwiseOperator.XOR: op = GeneratorAdapter.XOR; break;
                    default: op = -1;
                }

                String lhsResultType = populateMethodExpression(generator, lhs, ifFalseLabel, reifyCondition);
                String rhsResultType = populateMethodExpression(generator, rhs, ifFalseLabel, reifyCondition);
                String resultType = Factory.bitwiseResultType(lhsResultType, rhsResultType);
                Type t = Type.getType(resultType);
                generator.math(op, t);

                return resultType;
            } case ASTType.COMPARE: {
                Tuple lhs = (Tuple) expression.getPropertyValue(Property.KEY_LHS);
                Tuple rhs = (Tuple) expression.getPropertyValue(Property.KEY_RHS);

                int operator = expression.getIntProperty(Property.KEY_OPERATOR);
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

                return Descriptor.BOOLEAN;
            } case ASTType.LOGICAL: {
                Tuple lhs = (Tuple)expression.getPropertyValue(Property.KEY_LHS);
                Tuple rhs = (Tuple)expression.getPropertyValue(Property.KEY_RHS);

                int operator = expression.getIntProperty(Property.KEY_OPERATOR);
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

                        resultType = Factory.logicalResultType(lhsResultType, rhsResultType);

                        break;
                    }
                    case LogicalOperator.OR: {
                        Label endLabel = generator.newLabel();
                        Label nextTestLabel = generator.newLabel();
                        String lhsResultType = populateMethodExpression(generator, lhs, nextTestLabel, reifyCondition);
                        generator.goTo(endLabel);
                        generator.visitLabel(nextTestLabel);
                        String rhsResultType = populateMethodExpression(generator, rhs, ifFalseLabel, reifyCondition);
                        generator.visitLabel(endLabel);
                        resultType = Factory.logicalResultType(lhsResultType, rhsResultType);

                        break;
                    }
                }

                return resultType;
            } case ASTType.VARIABLE_ACCESS: {
                String name = expression.getStringProperty(Property.KEY_NAME);
                int id = methodScope.getVarId(name);
                generator.loadLocal(id);

                return methodScope.getVarType(name);
            } case ASTType.NOT: {
                Tuple bExpression = expression.getTupleProperty(Property.KEY_EXPRESSION);

                String resultType = populateMethodExpression(generator, bExpression, null, true);

                if(reifyCondition)
                    generator.not();
                if(ifFalseLabel != null)
                    generator.ifZCmp(GeneratorAdapter.NE, ifFalseLabel);

                return Descriptor.BOOLEAN;
            } case ASTType.INSTANCE_OF: {
                Tuple oExpression = expression.getTupleProperty(Property.KEY_EXPRESSION);
                String type = expression.getStringProperty(Property.KEY_TYPE);

                String resultType = populateMethodExpression(generator, oExpression, null, true);
                Type t = Type.getType(type);

                generator.instanceOf(t);

                return Descriptor.BOOLEAN;
            } case ASTType.BLOCK: {
                // Exactly one expression should be contained with statements
                List<Node> statements = (List<Node>) expression.getPropertyValueAs(Property.KEY_STATEMENTS, List.class);
                List<String> expressionResultTypes = new ArrayList<>();

                LabelScope labelScope = new LabelScope();

                statements.forEach(s -> {
                    // Try as expression
                    String resultType = populateMethodExpression(generator, (Tuple) s, null, true);

                    if(resultType != null) {
                        // Was an expression
                        expressionResultTypes.add(resultType);
                    } else {
                        // Try as statement
                        populateMethodStatement(generator, (Tuple) s, null, labelScope);
                    }
                });

                labelScope.verify();

                if(expressionResultTypes.size() > 1)
                    throw new IllegalArgumentException("Expression block has multiple expressions.");
                else if(expressionResultTypes.isEmpty())
                    throw new IllegalArgumentException("Expression block has no expressions.");

                return expressionResultTypes.get(0);
            } case ASTType.IF_ELSE: {
                Tuple condition = expression.getTupleProperty(Property.KEY_CONDITION);
                Tuple ifTrue = expression.getTupleProperty(Property.KEY_IF_TRUE);
                Tuple ifFalse = expression.getTupleProperty(Property.KEY_IF_FALSE);

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

                return ifTrueResultType;
            } case ASTType.INVOCATION: {
                return populateMethodInvocation(generator, methodScope, expression, CODE_LEVEL_EXPRESSION);
            } case ASTType.NEW_INSTANCE: {
                return populateMethodNewInstance(generator, methodScope, expression, CODE_LEVEL_EXPRESSION);
            }
        }

        return null; // Not an expression
    }

    private static final int CODE_LEVEL_STATEMENT = 0;
    private static final int CODE_LEVEL_EXPRESSION = 1;

    private String populateMethodInvocation(GeneratorAdapter generator, GenerateScope methodScope, Tuple ast, int codeLevel) {
        int invocation = ast.getIntProperty(Property.KEY_INVOCATION);
        String type = ast.getStringProperty(Property.KEY_TYPE);
        String name = ast.getStringProperty(Property.KEY_NAME);
        String descriptor = ast.getStringProperty(Property.KEY_DESCRIPTOR);
        String returnType = descriptor.substring(descriptor.indexOf(")") + 1);

        if(codeLevel == CODE_LEVEL_EXPRESSION && returnType.equals(Descriptor.VOID))
            throw new IllegalArgumentException("Invocations at expression level must return non-void value.");

        // Push target for instance invocations
        switch (invocation) {
            case Invocation.INTERFACE:
            case Invocation.VIRTUAL:
                Tuple target = ast.getTupleProperty(Property.KEY_TARGET);
                populateMethodExpression(generator, target, null, true);
                break;
        }

        List<Node> arguments = (List<Node>) ast.getPropertyValueAs(Property.KEY_ARGUMENTS, List.class);

        arguments.forEach(a ->
                populateMethodExpression(generator, (Tuple) a, null, true));

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

    private String populateMethodNewInstance(GeneratorAdapter generator, GenerateScope methodScope, Tuple ast, int codeLevel) {
        String type = ast.getStringProperty(Property.KEY_TYPE);
        List<String> parameterTypes = (List<String>)ast.getPropertyValueAs(Property.KEY_PARAMETER_TYPES, List.class);
        String returnType = type;
        List<Node> arguments = (List<Node>) ast.getPropertyValueAs(Property.KEY_ARGUMENTS, List.class);

        generator.newInstance(Type.getType(type));
        generator.dup();
        arguments.forEach(a ->
            populateMethodExpression(generator, (Tuple) a, null, true));
        generator.invokeConstructor(Type.getType(type), new Method("<init>", Descriptor.getMethodDescriptor(parameterTypes, Descriptor.VOID)));

        if(codeLevel == CODE_LEVEL_STATEMENT) {
            generator.pop();
            returnType = Descriptor.VOID;
        }

        return returnType;
    }
}

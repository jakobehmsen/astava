package astava.java.gen;

import astava.core.Node;
import astava.core.Tuple;
import astava.debug.Debug;
import astava.java.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.List;

public class ClassGenerator {
    private Tuple ast;

    public ClassGenerator(Node ast) {
        this.ast = (Tuple)ast;
    }

    public void populate(ClassNode classNode) {
        switch(getType(ast)) {
            case ASTType.CLASS:
                int modifier = ast.getIntProperty(Property.KEY_MODIFIER);
                String className = getClassName();
                String superName = ast.getStringProperty(Property.KEY_SUPER_NAME);

                classNode.version = Opcodes.V1_8;
                classNode.access = modifier;
                classNode.name = className;
                classNode.signature = "L" + className + ";";
                classNode.superName = superName;

                Tuple members = ast.getTupleProperty(Property.KEY_MEMBERS);

                members.getTuples().forEach(m -> populateMember(classNode, m));

                break;
            default:
                break;
        }
    }

    public String getClassName() {
        return ast.getStringProperty(Property.KEY_NAME);
    }

    private String getDescriptor(String typeName) {
        switch(typeName) {
            case "V":
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
            case "F":
            case "J":
            case "D":
                return typeName;
        }

        return "L" + typeName + ";";
    }

    private String getMethodDescriptor(List<String> parameterTypeNames, String returnTypeName) {
        StringBuilder mdBuilder = new StringBuilder();

        mdBuilder.append("(");
        for(int i = 0; i < parameterTypeNames.size(); i++) {
            String ptn = parameterTypeNames.get(i);
            mdBuilder.append(getDescriptor(ptn));
        }
        mdBuilder.append(")");
        mdBuilder.append(getDescriptor(returnTypeName));

        return mdBuilder.toString();

        //return "(" + parameterTypeNames.stream().map(ptn -> getDescriptor(ptn)) + ")" + getDescriptor(returnTypeName);
    }

    public void populateMember(ClassNode classNode, Tuple member) {
        switch(getType(member)) {
            case ASTType.METHOD:
                int modifier = member.getIntProperty(Property.KEY_MODIFIER);
                String methodName = member.getStringProperty(Property.KEY_NAME);
                List<String> parameterTypeNames = (List<String>)member.getPropertyValueAs(Property.KEY_PARAMETER_TYPES, List.class);
                Type[] parameterTypes = new Type[parameterTypeNames.size()];
                for(int i = 0; i < parameterTypeNames.size(); i++)
                    parameterTypes[i] = Type.getType(parameterTypeNames.get(i));
                String returnTypeName = member.getStringProperty(Property.KEY_RETURN_TYPE);

                //String methodDescriptor = Type.getMethodDescriptor(Type.getType(returnType), parameterTypes);
                String methodDescriptor = getMethodDescriptor(parameterTypeNames, returnTypeName);
                MethodNode methodNode = new MethodNode(Opcodes.ASM5, modifier, methodName, methodDescriptor, null, null);

                Tuple body = (Tuple)member.getPropertyValue(Property.KEY_BODY);

                Method m = new Method(methodName, methodNode.desc);
                GeneratorAdapter generator = new GeneratorAdapter(modifier, m, methodNode);

                generator.visitCode();
                populateMethodStatement(generator, new Scope(), body, null, null);
                generator.visitEnd();
                generator.visitMaxs(0, 0);

                classNode.methods.add(methodNode);

                break;
            default:
                break;
        }
    }

    public void populateMethodStatement(GeneratorAdapter generator, Scope methodScope, Tuple statement, Label breakLabel, Label continueLabel) {
        switch(getType(statement)) {
            case ASTType.RETURN_STATEMENT:
                Tuple expression = statement.getTupleProperty(Property.KEY_EXPRESSION);

                populateMethodExpression(generator, methodScope, expression, false, null, true);

                generator.returnValue();

                break;
            case ASTType.BLOCK:
                List<Node> statements = (List<Node>) statement.getPropertyValueAs(Property.KEY_STATEMENTS, List.class);

                statements.forEach(s -> populateMethodStatement(generator, methodScope, (Tuple) s, breakLabel, continueLabel));

                break;
            case ASTType.IF_ELSE: {
                Tuple condition = statement.getTupleProperty(Property.KEY_CONDITION);
                Tuple ifTrue = statement.getTupleProperty(Property.KEY_IF_TRUE);
                Tuple ifFalse = statement.getTupleProperty(Property.KEY_IF_FALSE);

                Label endLabel = generator.newLabel();
                Label ifFalseLabel = generator.newLabel();

                String resultType = populateMethodExpression(generator, methodScope, condition, false, ifFalseLabel, false);
                populateMethodStatement(generator, methodScope, ifTrue, breakLabel, continueLabel);
                generator.goTo(endLabel);
                generator.visitLabel(ifFalseLabel);
                populateMethodStatement(generator, methodScope, ifFalse, breakLabel, continueLabel);
                generator.visitLabel(endLabel);
                break;
            } case ASTType.LOOP: {
                Tuple condition = statement.getTupleProperty(Property.KEY_CONDITION);
                Tuple body = statement.getTupleProperty(Property.KEY_BODY);

                Label startLabel = generator.newLabel();
                Label endLabel = generator.newLabel();

                generator.visitLabel(startLabel);
                String resultType = populateMethodExpression(generator, methodScope, condition, false, endLabel, false);
                populateMethodStatement(generator, methodScope, body, endLabel, startLabel);
                generator.goTo(startLabel);
                generator.visitLabel(endLabel);

                break;
            } case ASTType.BREAK: {
                generator.goTo(breakLabel);
                break;
            } case ASTType.CONTINUE: {
                generator.goTo(continueLabel);
                break;
            } default: {
                // Assumed to be root expression
                populateMethodExpression(generator, methodScope, statement, true, null, false);
            }
        }
    }

    public String populateMethodExpression(GeneratorAdapter generator, Scope methodScope, Tuple expression, boolean isRoot, Label ifFalseLabel, boolean reifyCondition) {
        switch(getType(expression)) {
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

                String lhsResultType = populateMethodExpression(generator, methodScope, lhs, false, ifFalseLabel, false);
                String rhsResultType = populateMethodExpression(generator, methodScope, rhs, false, ifFalseLabel, reifyCondition);

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

                String lhsResultType = populateMethodExpression(generator, methodScope, lhs, false, ifFalseLabel, reifyCondition);
                String rhsResultType = populateMethodExpression(generator, methodScope, rhs, false, ifFalseLabel, reifyCondition);
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

                String lhsResultType = populateMethodExpression(generator, methodScope, lhs, false, ifFalseLabel, reifyCondition);
                String rhsResultType = populateMethodExpression(generator, methodScope, rhs, false, ifFalseLabel, reifyCondition);
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

                String lhsResultType = populateMethodExpression(generator, methodScope, lhs, false, ifFalseLabel, reifyCondition);
                String rhsResultType = populateMethodExpression(generator, methodScope, rhs, false, ifFalseLabel, reifyCondition);

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
                        String lhsResultType = populateMethodExpression(generator, methodScope, lhs, false, lhsIfFalseLabel, lhsReify);
                        String rhsResultType = populateMethodExpression(generator, methodScope, rhs, false, ifFalseLabel, reifyCondition);

                        if(ifFalseLabel == null) {
                            generator.visitLabel(lhsIfFalseLabel);
                        }

                        resultType = Factory.logicalResultType(lhsResultType, rhsResultType);

                        break;
                    }
                    case LogicalOperator.OR: {
                        Label endLabel = generator.newLabel();
                        Label nextTestLabel = generator.newLabel();
                        String lhsResultType = populateMethodExpression(generator, methodScope, lhs, false, nextTestLabel, reifyCondition);
                        generator.goTo(endLabel);
                        generator.visitLabel(nextTestLabel);
                        String rhsResultType = populateMethodExpression(generator, methodScope, rhs, false, ifFalseLabel, reifyCondition);
                        generator.visitLabel(endLabel);
                        resultType = Factory.logicalResultType(lhsResultType, rhsResultType);

                        break;
                    }
                }

                return resultType;
            } case ASTType.VARIABLE_DECLARATION: {
                String type = expression.getStringProperty(Property.KEY_VAR_TYPE);
                String name = expression.getStringProperty(Property.KEY_NAME);
                methodScope.declareVar(generator, type, name);

                return Descriptor.STRING;
            } case ASTType.VARIABLE_ASSIGNMENT: {
                String name = expression.getStringProperty(Property.KEY_NAME);
                Tuple value = expression.getTupleProperty(Property.KEY_EXPRESSION);
                String valueType = populateMethodExpression(generator, methodScope, value, false, null, true);
                int id = methodScope.getVarId(name);
                generator.storeLocal(id, Type.getType(valueType));
                if(!isRoot)
                    generator.loadLocal(id, Type.getType(valueType));

                return methodScope.getVarType(name);
            } case ASTType.VARIABLE_ACCESS: {
                String name = expression.getStringProperty(Property.KEY_NAME);
                int id = methodScope.getVarId(name);
                generator.loadLocal(id);

                return methodScope.getVarType(name);
            } case ASTType.INCREMENT: {
                String name = expression.getStringProperty(Property.KEY_NAME);
                int timing = expression.getIntProperty(Property.KEY_TIMING);
                int amount = expression.getIntProperty(Property.KEY_AMOUNT);
                int id = methodScope.getVarId(name);

                if(timing == IncTiming.PRE)
                    generator.iinc(id, amount);

                if(!isRoot)
                    generator.loadLocal(id);

                if(timing == IncTiming.POST)
                    generator.iinc(id, amount);

                return methodScope.getVarType(name);
            } case ASTType.NOT: {
                Tuple bExpression = expression.getTupleProperty(Property.KEY_EXPRESSION);

                String resultType = populateMethodExpression(generator, methodScope, bExpression, false, null, true);

                if(reifyCondition)
                    generator.not();
                if(ifFalseLabel != null)
                    generator.ifZCmp(GeneratorAdapter.NE, ifFalseLabel);

                return Descriptor.BOOLEAN;
            }
        }

        return null;
    }

    public byte[] toBytes() {
        ClassNode classNode = new ClassNode(Opcodes.ASM5);

        populate(classNode);

        classNode.accept(new TraceClassVisitor(new PrintWriter(Debug.getPrintStream(Debug.LEVEL_HIGH))));

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);

        org.objectweb.asm.util.CheckClassAdapter.verify(new ClassReader(classWriter.toByteArray()), true, new PrintWriter(Debug.getPrintStream(Debug.LEVEL_HIGH)));

        return classWriter.toByteArray();
    }

    public ClassLoader newClassLoader() {
        return new SingleClassLoader(this);
    }

    public Class<?> newClass() throws ClassNotFoundException {
        return newClassLoader().loadClass(getClassName());
    }

    private int getType(Tuple n) {
        return n.getIntProperty(Property.KEY_AST_TYPE);
    }
}

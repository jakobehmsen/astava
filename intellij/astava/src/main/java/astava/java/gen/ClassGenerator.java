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
                populateMethodStatements(generator, new Scope() , body);
                generator.visitEnd();
                generator.visitMaxs(0, 0);

                classNode.methods.add(methodNode);

                break;
            default:
                break;
        }
    }

    public void populateMethodStatements(GeneratorAdapter generator, Scope methodScope, Tuple statement) {
        switch(getType(statement)) {
            case ASTType.RETURN_STATEMENT:
                Tuple expression = statement.getTupleProperty(Property.KEY_EXPRESSION);

                populateMethodExpression(generator, methodScope, expression, false);

                generator.returnValue();

                break;
            case ASTType.BLOCK:
                List<Node> statements = (List<Node>) statement.getPropertyValueAs(Property.KEY_STATEMENTS, List.class);

                statements.forEach(s -> populateMethodStatements(generator, methodScope, (Tuple) s));

                break;
            default: {
                // Assumed to be root expression
                populateMethodExpression(generator, methodScope, statement, true);
            }
        }
    }

    public String populateMethodExpression(GeneratorAdapter generator, Scope methodScope, Tuple expression, boolean isRoot) {
        switch(getType(expression)) {
            case ASTType.BOOLEAN_LITERAL: {
                boolean value = expression.getBooleanProperty(Property.KEY_VALUE);
                generator.push(value);

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
                //String resultType = expression.getStringProperty(Property.KEY_RESULT_TYPE);
                //Type t = Type.getType(resultType);
                int operator = expression.getIntProperty(Property.KEY_OPERATOR);
                int op;

                switch(operator) {
                    case ReduceOperator.ADD: op = GeneratorAdapter.ADD; break;
                    case ReduceOperator.SUB: op = GeneratorAdapter.SUB; break;
                    case ReduceOperator.MUL: op = GeneratorAdapter.MUL; break;
                    case ReduceOperator.DIV: op = GeneratorAdapter.DIV; break;
                    case ReduceOperator.REM: op = GeneratorAdapter.REM; break;
                    case ReduceOperator.SHL: op = GeneratorAdapter.SHL; break;
                    case ReduceOperator.SHR: op = GeneratorAdapter.SHR; break;
                    case ReduceOperator.USHR: op = GeneratorAdapter.USHR; break;
                    default: op = -1;
                }

                String lhsResultType = populateMethodExpression(generator, methodScope, lhs, false);
                String rhsResultType = populateMethodExpression(generator, methodScope, rhs, false);
                //String operandType = Factory.operandsType(operator, lhsResultType, rhsResultType);
                String resultType = Factory.resultType(operator, lhsResultType, rhsResultType);
                Type t = Type.getType(resultType);
                generator.math(op, t);

                return resultType;
            } case ASTType.VARIABLE_DECLARATION: {
                String type = expression.getStringProperty(Property.KEY_VAR_TYPE);
                String name = expression.getStringProperty(Property.KEY_NAME);
                methodScope.declareVar(generator, type, name);

                return Descriptor.STRING;
            } case ASTType.VARIABLE_ASSIGNMENT: {
                String name = expression.getStringProperty(Property.KEY_NAME);
                Tuple value = expression.getTupleProperty(Property.KEY_EXPRESSION);
                String valueType = populateMethodExpression(generator, methodScope, value, false);
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

                if(timing == IncDec.TIMING_PRE)
                    generator.iinc(id, amount);

                generator.loadLocal(id);

                if(timing == IncDec.TIMING_POST)
                    generator.iinc(id, amount);

                return methodScope.getVarType(name);
            } case ASTType.NOT: {
                // Should be sensitive to false-labels
                Tuple bExpression = expression.getTupleProperty(Property.KEY_EXPRESSION);

                String resultType = populateMethodExpression(generator, methodScope, bExpression, false);

                generator.not();

                return Descriptor.BOOLEAN;
            }case ASTType.COMPARE:
                // Should be sensitive to false-labels
                Tuple lhs = (Tuple)expression.getPropertyValue(Property.KEY_LHS);
                Tuple rhs = (Tuple)expression.getPropertyValue(Property.KEY_RHS);

                int operator = expression.getIntProperty(Property.KEY_OPERATOR);
                int op;

                switch(operator) {
                    case RelationalOperator.LT: op = GeneratorAdapter.GE; break;
                    case RelationalOperator.LE: op = GeneratorAdapter.GT; break;
                    case RelationalOperator.GT: op = GeneratorAdapter.LE; break;
                    case RelationalOperator.GE: op = GeneratorAdapter.LT; break;
                    case RelationalOperator.EQ: op = GeneratorAdapter.NE; break;
                    case RelationalOperator.NE: op = GeneratorAdapter.EQ; break;
                    default: op = -1;
                }

                String lhsResultType = populateMethodExpression(generator, methodScope, lhs, false);
                String rhsResultType = populateMethodExpression(generator, methodScope, rhs, false);

                /*

                ILOAD 0
                ILOAD 1
                IF_ICMPGE L1
                ICONST_1
                GOTO L2
               L1
                ICONST_0
               L2

                */

                Type t = Type.getType(lhsResultType);

                Label endLabel = generator.newLabel();
                Label ifFalseLabel = generator.newLabel();
                //generator.math(op, t);
                generator.ifCmp(t, op, ifFalseLabel);

                generator.push(true);
                generator.goTo(endLabel);
                generator.visitLabel(ifFalseLabel);
                generator.push(false);
                generator.visitLabel(endLabel);

                return Descriptor.BOOLEAN;

        }

        return null;
    }

    public byte[] toBytes() {
        ClassNode classNode = new ClassNode(Opcodes.ASM5);

        populate(classNode);

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

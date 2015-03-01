package astava.java.gen;

import astava.core.Node;
import astava.core.Tuple;
import astava.java.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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
                populateMethodStatements(body, generator);
                generator.visitEnd();
                generator.visitMaxs(0, 0);

                classNode.methods.add(methodNode);

                break;
            default:
                break;
        }
    }

    public void populateMethodStatements(Tuple statement, GeneratorAdapter generator) {
        switch(getType(statement)) {
            case ASTType.RETURN_STATEMENT:
                Tuple expression = statement.getTupleProperty(Property.KEY_EXPRESSION);

                populateMethodExpression(expression, generator, false);

                generator.returnValue();

                break;
        }
    }

    public String populateMethodExpression(Tuple expression, GeneratorAdapter generator, boolean isRoot) {
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
            }  case ASTType.DOUBLE_LITERAL: {
                double value = expression.getDoubleProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.DOUBLE;
            }  case ASTType.CHAR_LITERAL: {
                char value = expression.getCharProperty(Property.KEY_VALUE);
                generator.push(value);

                return Descriptor.CHAR;
            }  case ASTType.STRING_LITERAL: {
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
                    default: op = -1;
                }

                String lhsResultType = populateMethodExpression(lhs, generator, false);
                String rhsResultType = populateMethodExpression(rhs, generator, false);
                String resultType = Factory.resultType(lhsResultType, rhsResultType);
                Type t = Type.getType(resultType);
                generator.math(op, t);

                break;
            }
        }

        return null;
    }

    public byte[] toBytes() {
        ClassNode classNode = new ClassNode(Opcodes.ASM5);

        populate(classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);

        //org.objectweb.asm.util.CheckClassAdapter.verify(new ClassReader(classWriter.toByteArray()), true, new PrintWriter(System.out));

        return classWriter.toByteArray();
    }

    public ClassLoader newClassLoader() {
        return new SingleClassLoader(this);
    }

    public Class<?> newClass() throws ClassNotFoundException {
        return newClassLoader().loadClass(getClassName());
    }

    private int getType(Tuple n) {
        return n.getIntProperty(Property.KEY_TYPE);
    }
}

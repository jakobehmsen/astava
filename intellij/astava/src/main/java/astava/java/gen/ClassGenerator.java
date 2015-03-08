package astava.java.gen;

import astava.core.Node;
import astava.core.Tuple;
import astava.debug.Debug;
import astava.java.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.TableSwitchGenerator;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassGenerator {
    private Tuple ast;

    public ClassGenerator(Node ast) {
        this.ast = (Tuple)ast;
    }

    public void populate(ClassNode classNode) {
        switch(ast.getIntProperty(Property.KEY_AST_TYPE)) {
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

    public void populateMember(ClassNode classNode, Tuple member) {
        switch(member.getIntProperty(Property.KEY_AST_TYPE)) {
            case ASTType.METHOD:
                int modifier = member.getIntProperty(Property.KEY_MODIFIER);
                String methodName = member.getStringProperty(Property.KEY_NAME);
                List<String> parameterTypeNames = (List<String>)member.getPropertyValueAs(Property.KEY_PARAMETER_TYPES, List.class);
                Type[] parameterTypes = new Type[parameterTypeNames.size()];
                for(int i = 0; i < parameterTypeNames.size(); i++)
                    parameterTypes[i] = Type.getType(parameterTypeNames.get(i));
                String returnTypeName = member.getStringProperty(Property.KEY_RETURN_TYPE);

                String methodDescriptor = Descriptor.getMethodDescriptor(parameterTypeNames, returnTypeName);
                MethodNode methodNode = new MethodNode(Opcodes.ASM5, modifier, methodName, methodDescriptor, null, null);

                Tuple body = (Tuple)member.getPropertyValue(Property.KEY_BODY);

                Method m = new Method(methodName, methodNode.desc);
                GeneratorAdapter generator = new GeneratorAdapter(modifier, m, methodNode);

                MethodGenerator methodGenerator = new MethodGenerator(body);

                methodGenerator.generate(generator);

                classNode.methods.add(methodNode);

                break;
            default:
                break;
        }
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
}

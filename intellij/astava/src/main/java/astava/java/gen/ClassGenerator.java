package astava.java.gen;

import astava.tree.Node;
import astava.tree.Tuple;
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

import static astava.java.Factory.*;

public class ClassGenerator {
    private Tuple ast;

    public ClassGenerator(Node ast) {
        this.ast = (Tuple)ast;
    }

    public void populate(ClassNode classNode) {
        switch(astType(ast)) {
            case ASTType.CLASS:
                int modifier = classDeclarationModifier(ast);
                String className = getClassName();
                String superName = classDeclarationSuperName(ast);
                List<Node> members = classDeclarationMembers(ast);

                classNode.version = Opcodes.V1_8;
                classNode.access = modifier;
                classNode.name = className;
                classNode.signature = "L" + className + ";";
                classNode.superName = superName;

                members.forEach(m -> populateMember(classNode, (Tuple) m));

                break;
            default:
                break;
        }
    }

    public String getClassName() {
        return classDeclarationName(ast);
    }

    public void populateMember(ClassNode classNode, Tuple member) {
        switch(astType(member)) {
            case ASTType.METHOD:
                int modifier = methodDeclarationModifier(member);
                String methodName = methodDeclarationName(member);
                List<String> parameterTypeNames =  methodDeclarationParameterTypes(member);
                String returnTypeName = methodDeclarationReturnType(member);
                Tuple body = methodDeclarationBody(member);

                Type[] parameterTypes = new Type[parameterTypeNames.size()];
                for(int i = 0; i < parameterTypeNames.size(); i++)
                    parameterTypes[i] = Type.getType(parameterTypeNames.get(i));

                String methodDescriptor = Descriptor.getMethodDescriptor(parameterTypeNames, returnTypeName);
                MethodNode methodNode = new MethodNode(Opcodes.ASM5, modifier, methodName, methodDescriptor, null, null);

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

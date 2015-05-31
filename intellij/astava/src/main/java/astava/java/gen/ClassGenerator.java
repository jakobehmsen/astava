package astava.java.gen;

import astava.debug.Debug;
import astava.java.Descriptor;
import astava.tree.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.List;

public class ClassGenerator {
    private ClassDom classDom;

    public ClassGenerator(ClassDom classDom) {
        this.classDom = classDom;
    }

    public void populate(ClassNode classNode) {
        int modifiers = classDom.getModifiers();
        String className = classDom.getName();
        String superName = classDom.getSuperName();

        classNode.version = Opcodes.V1_8;
        classNode.access = modifiers;
        classNode.name = className;
        classNode.signature = "L" + className + ";";
        classNode.superName = superName;

        classDom.getFields().forEach(f -> populateField(classNode, f));
        classDom.getMethods().forEach(m -> populateMethod(classNode, m));
    }

    public void populateField(ClassNode classNode, FieldDom fieldDom) {
        String descriptor = Descriptor.getFieldDescriptor(fieldDom.getTypeName());
        FieldNode f = new FieldNode(fieldDom.getModifiers(), fieldDom.getName(), descriptor, null, null);

        classNode.fields.add(f);
    }

    public void populateMethod(ClassNode classNode, MethodDom methodDom) {
        int modifiers = methodDom.getModifiers();
        String methodName = methodDom.getName();
        List<String> parameterTypeNames = methodDom.getParameterTypes();
        String returnTypeName = methodDom.getReturnTypeName();
        StatementDom body = methodDom.getBody();

        Type[] parameterTypes = new Type[parameterTypeNames.size()];
        for(int i = 0; i < parameterTypeNames.size(); i++)
            parameterTypes[i] = Type.getType(parameterTypeNames.get(i));

        String methodDescriptor = Descriptor.getMethodDescriptor(parameterTypeNames, returnTypeName);
        MethodNode methodNode = new MethodNode(Opcodes.ASM5, modifiers, methodName, methodDescriptor, null, null);

        Method m = new Method(methodName, methodNode.desc);
        GeneratorAdapter generator = new GeneratorAdapter(modifiers, m, methodNode);

        MethodGenerator methodGenerator = new MethodGenerator(this, body);

        methodGenerator.generate(generator);

        classNode.methods.add(methodNode);
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

    public String getClassName() {
        return classDom.getName();
    }
}

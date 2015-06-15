package astava.java.gen;

import astava.debug.Debug;
import astava.java.Descriptor;
import astava.tree.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClassGenerator {
    private ClassDom classDom;

    public ClassGenerator(ClassDom classDom) {
        this.classDom = classDom;
    }

    public void populate(ClassNode classNode) {
        int modifiers = classDom.getModifiers();
        String className = Descriptor.get(classDom.getName());
        String superName = Descriptor.get(classDom.getSuperName());

        classNode.version = Opcodes.V1_8;
        classNode.access = modifiers;
        classNode.name = className;
        classNode.signature = "L" + className + ";";
        classNode.superName = superName;

        classDom.getInterfaces().forEach(x -> classNode.interfaces.add(x));

        classDom.getFields().forEach(f -> populateField(classNode, f));
        classDom.getMethods().forEach(m -> populateMethod(classNode, m));
    }

    public void populate2(ClassNode classNode) {
        /*int modifiers = classDom.getModifiers();
        String className = Descriptor.get(classDom.getName());
        String superName = Descriptor.get(classDom.getSuperName());

        classNode.version = Opcodes.V1_8;
        classNode.access = modifiers;
        classNode.name = className;
        classNode.signature = "L" + className + ";";
        classNode.superName = superName;*/

        classDom.getInterfaces().forEach(x -> classNode.interfaces.add(x));

        classNode.fields.stream()
            .filter(x -> classDom.getFields().stream().anyMatch(y -> y.getName().equals(((FieldNode) x).name)))
            .forEach(x -> classNode.fields.remove(x));

        List<MethodNode> classNodeMethods = (List<MethodNode>)classNode.methods.stream().collect(Collectors.toList());
        //classNode.methods.stream()
        classNodeMethods.stream()
            .filter(x -> classDom.getMethods().stream().anyMatch(y ->
            {
                if (y == null || y.getName() == null || x == null || ((MethodNode) x).name == null)
                    new String();

                if (y.getName().equals(((MethodNode) x).name)) {
                    Type[] xParameterTypes = Type.getArgumentTypes(x.desc);
                    if (y.getParameterTypes().size() == xParameterTypes.length) {
                        return IntStream.range(0, y.getParameterTypes().size()).allMatch(i -> {
                            String xParameterType = Descriptor.get(xParameterTypes[i].getClassName());
                            return y.getParameterTypes().get(i).descriptor.equals(xParameterType);
                        });
                    }
                }

                return false;
            }))
            .forEach(x -> classNode.methods.remove(x));

        classDom.getFields().forEach(f -> populateField(classNode, f));
        classDom.getMethods().forEach(m -> populateMethod(classNode, m));
    }

    public void populateField(ClassNode classNode, FieldDom fieldDom) {
        FieldNode f = fieldDom.accept(new FieldDomVisitor<FieldNode>() {
            @Override
            public FieldNode visitCustomField(CustomFieldDom fieldDom) {
                String descriptor = Descriptor.getFieldDescriptor(fieldDom.getTypeName());
                return new FieldNode(fieldDom.getModifiers(), fieldDom.getName(), descriptor, null, null);
            }

            @Override
            public FieldNode visitASMField(FieldNode fieldNode) {
                return fieldNode;
            }
        });

        //String descriptor = Descriptor.getFieldDescriptor(fieldDom.getTypeName());
        //FieldNode f = new FieldNode(fieldDom.getModifiers(), fieldDom.getName(), descriptor, null, null);

        classNode.fields.add(f);
    }

    public void populateMethod(ClassNode classNode, MethodDom methodDom) {
        MethodNode methodNode = new StatementDomVisitor.Return<MethodNode>() {
            @Override
            public void visitASM(MethodNode methodNode) {
                setResult(methodNode);
            }
        }.returnFrom(methodDom.getBody());

        if(methodNode == null) {
            int modifiers = methodDom.getModifier();
            String methodName = methodDom.getName();
            List<ParameterInfo> parameters = methodDom.getParameterTypes();
            String returnTypeName = methodDom.getReturnTypeName();
            StatementDom body = methodDom.getBody();

            Type[] parameterTypes = new Type[parameters.size()];
            for(int i = 0; i < parameters.size(); i++)
                parameterTypes[i] = Type.getType(parameters.get(i).descriptor);

            List<String> parameterTypeNames = parameters.stream().map(x -> x.descriptor).collect(Collectors.toList());
            String methodDescriptor = Descriptor.getMethodDescriptor(parameterTypeNames, returnTypeName);
            methodNode = new MethodNode(Opcodes.ASM5, modifiers, methodName, methodDescriptor, null, null);

            Method m = new Method(methodName, methodNode.desc);
            //GeneratorAdapter generator = new GeneratorAdapter(modifiers, m, methodNode);

            MethodGenerator methodGenerator = new MethodGenerator(this, parameters, body);

            //methodGenerator.generate(generator);
            methodGenerator.generate(methodNode);
        }

        classNode.methods.add(methodNode);
    }

    public byte[] toBytes() {
        ClassNode classNode = new ClassNode(Opcodes.ASM5);

        populate(classNode);

        classNode.accept(new TraceClassVisitor(new PrintWriter(Debug.getPrintStream(Debug.LEVEL_HIGH))));

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);

        boolean hasASMMethodNodes = classDom.getMethods().stream().map(x -> new StatementDomVisitor.Return<MethodNode>() {
            @Override
            public void visitASM(MethodNode methodNode) {
                setResult(methodNode);
            }
        }.returnFrom(x.getBody())).anyMatch(x -> x != null);

        try {
            org.objectweb.asm.util.CheckClassAdapter.verify(new ClassReader(classWriter.toByteArray()), true, new PrintWriter(Debug.getPrintStream(Debug.LEVEL_HIGH)));
        } catch(Exception e) {
            if(!hasASMMethodNodes)
                throw e;
        }

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

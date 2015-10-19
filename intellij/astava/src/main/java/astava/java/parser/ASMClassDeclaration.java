package astava.java.parser;

import astava.java.Descriptor;
import astava.tree.FieldDom;
import astava.tree.MethodDom;
import astava.tree.ParameterInfo;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ASMClassDeclaration implements ClassDeclaration {
    private ClassNode classNode;

    public ASMClassDeclaration(ClassNode classNode) {
        this.classNode = classNode;
    }

    @Override
    public List<FieldDeclaration> getFields() {
        return getFields(classNode);
    }

    public static String getName(ClassNode classNode) {
        return Descriptor.getName(classNode.name);
    }

    public static List<FieldDeclaration> getFields(ClassNode classNode) {
        return ((List<FieldNode>)classNode.fields).stream().map(x -> fieldDeclaration(x)).collect(Collectors.toList());
    }

    private static FieldDeclaration fieldDeclaration(FieldNode fieldNode) {
        return new FieldDeclaration() {
            @Override
            public int getModifier() {
                return fieldNode.access;
            }

            @Override
            public String getTypeName() {
                return Type.getType(fieldNode.desc).getClassName();
            }

            @Override
            public String getName() {
                return fieldNode.name;
            }

            @Override
            public FieldDom build(ClassDeclaration classDeclaration) {
                return null;
            }
        };
    }

    @Override
    public List<MethodDeclaration> getMethods() {
        return getMethods(classNode);
    }

    public static List<MethodDeclaration> getMethods(ClassNode classNode) {
        return ((List<MethodNode>)classNode.methods).stream().map(x -> methodDeclaration(x)).collect(Collectors.toList());
    }

    private static MethodDeclaration methodDeclaration(MethodNode methodNode) {
        return new MethodDeclaration() {
            @Override
            public int getModifier() {
                return methodNode.access;
            }

            @Override
            public String getName() {
                return methodNode.name;
            }

            @Override
            public List<ParameterInfo> getParameterTypes() {
                Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
                return IntStream.range(0, argumentTypes.length).mapToObj(i -> new ParameterInfo(
                    argumentTypes[i].getDescriptor(),
                    methodNode.parameters != null ? ((ParameterNode)methodNode.parameters.get(i)).name : "arg" + i
                )).collect(Collectors.toList());
            }

            @Override
            public String getReturnTypeName() {
                return Descriptor.getName(Type.getReturnType(methodNode.desc).getDescriptor());
            }

            @Override
            public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                return null;
            }
        };
    }

    @Override
    public int getModifiers() {
        return classNode.access;
    }

    @Override
    public String getName() {
        return getName(classNode);
    }

    @Override
    public String getSuperName() {
        return Descriptor.getName(classNode.superName);
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public List<String> getInterfaces() {
        return (List<String>)classNode.interfaces;
    }
}

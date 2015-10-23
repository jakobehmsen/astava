package astava.java.agent;

import astava.java.Descriptor;
import astava.java.gen.MethodGenerator;
import astava.tree.*;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClassNodeExtenderFactory {
    public static DeclaringClassNodeExtenderTransformer setSuperName(String superName) {
        return (classNode, thisClass, classResolver, classInspector) -> classNode.superName = superName;
    }

    public static DeclaringClassNodeExtenderTransformer addAnnotation(String typeName, Map<String, Object> values) {
        return (classNode, thisClass, classResolver, classInspector) -> {
            String desc = Descriptor.getTypeDescriptor(typeName);
            //new Annotation();
            //classNode.visibleTypeAnnotations.add(new AnnotationNode(desc));
            //classNode.visibleAnnotations.add(new AnnotationNode(desc));
            AnnotationVisitor annotation = classNode.visitAnnotation(desc, true);
            values.entrySet().stream().forEach(v -> annotation.visit(v.getKey(), v.getValue()));
        };
    }

    public static DeclaringClassNodeExtenderTransformer addField(FieldDom fieldDom) {
        return (classNode, thisClass, classResolver, classInspector) -> {
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

            classNode.fields.add(f);
        };
    }

    public static DeclaringClassNodeExtenderTransformer addMethod(MethodDom methodDom) {
        return (classNode, thisClass, classResolver, classInspector) -> {
            MethodNode methodNode = new StatementDomVisitor.Return<MethodNode>() {
                @Override
                public void visitASM(MethodNode methodNode) {
                    setResult(methodNode);
                }
            }.returnFrom(methodDom.getBody());

            if (methodNode == null) {
                int modifiers = methodDom.getModifier();
                String methodName = methodDom.getName();
                List<ParameterInfo> parameters = methodDom.getParameterTypes();
                String returnTypeName = methodDom.getReturnTypeName();
                StatementDom body = methodDom.getBody();

                Type[] parameterTypes = new Type[parameters.size()];
                for (int i = 0; i < parameters.size(); i++)
                    parameterTypes[i] = Type.getType(parameters.get(i).descriptor);

                List<String> parameterTypeNames = parameters.stream().map(x -> x.descriptor).collect(Collectors.toList());
                String methodDescriptor = Descriptor.getMethodDescriptor(parameterTypeNames, returnTypeName);
                methodNode = new MethodNode(Opcodes.ASM5, modifiers, methodName, methodDescriptor, null, null);

                Method m = new Method(methodName, methodNode.desc);
                //GeneratorAdapter generator = new GeneratorAdapter(modifiers, m, methodNode);

                MethodGenerator methodGenerator = new MethodGenerator(classNode.name, parameters, body);

                //methodGenerator.generate(generator);
                methodGenerator.generate(methodNode);
            }

            //System.out.println(classNode.name);
            IntStream.range(0, classNode.methods.size()).filter(i -> methodDom.getName().equals(((MethodNode) classNode.methods.get(i)).name)).findAny().ifPresent(i ->
                    classNode.methods.remove(i)
            );
        /*classNode.methods.stream()
            .filter(x ->
                methodDom.getName().equals(((MethodNode) x).name))
            .forEach(x ->
                classNode.methods.remove(x));*/
            //System.out.println(classNode.name);

            classNode.methods.add(methodNode);
        };
    }
}

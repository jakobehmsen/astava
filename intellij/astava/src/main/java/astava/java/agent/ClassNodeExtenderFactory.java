package astava.java.agent;

import astava.java.Descriptor;
import astava.java.gen.MethodGenerator;
import astava.tree.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClassNodeExtenderFactory {
    public static ClassNodeExtender setSuperName(String superName) {
        return classNode -> {
            classNode.superName = superName;
        };
    }

    public static ClassNodeExtender addField(FieldDom fieldDom) {
        return classNode -> {
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

    public static ClassNodeExtender addMethod(MethodDom methodDom) {
        return classNode -> {
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

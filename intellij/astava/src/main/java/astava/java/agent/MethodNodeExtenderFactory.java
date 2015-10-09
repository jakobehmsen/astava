package astava.java.agent;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.java.gen.MethodGenerator;
import astava.tree.ParameterInfo;
import astava.tree.StatementDom;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class MethodNodeExtenderFactory {
    public static MethodNodeExtender sequence(MethodNodeExtender... extenders) {
        return (classNode, methodNode) -> {
            Arrays.asList(extenders).forEach(x -> x.transform(classNode, methodNode));
        };
    }

    public static MethodNodeExtender modify(MethodNodeBodyModifier modifier) {
        return (classNode, methodNode) -> {
            InsnList originalInstructions = new InsnList();
            originalInstructions.add(methodNode.instructions);
            methodNode.instructions.clear();

            MethodGenerator.generate(methodNode, (mn, generator) -> {
                modifier.modify(classNode, methodNode, generator, originalInstructions);

                /*Type[] argumentTypes = Type.getArgumentTypes(methodNode.signature);
                List<ParameterInfo> parameters = IntStream.range(0, argumentTypes.length).mapToObj(i -> new ParameterInfo(
                    argumentTypes[i].getDescriptor(),
                    ((ParameterNode)methodNode.parameters.get(i)).name
                )).collect(Collectors.toList());
                MethodGenerator methodGenerator = new MethodGenerator(classNode.name, parameters, code);
                methodGenerator.populateMethodBody(mn, generator);

                mn.instructions.add(originalInstructions);*/
            });
        };
    }

    public static MethodNodeExtender addBefore(StatementDom code) {
        return modify((classNode, methodNode, generator, originalInstructions) -> {
            System.out.println("addBefore@start");
            try {
                add(classNode, methodNode, generator, code);
                methodNode.instructions.add(originalInstructions);
            } catch(Exception e) {
                e.printStackTrace();
                throw e;
            }

            System.out.println("addBefore@end");
        });

        /*return (classNode, methodNode) -> {
            InsnList originalInstruction = new InsnList();
            originalInstruction.add(methodNode.instructions);
            methodNode.instructions.clear();

            MethodGenerator.generate(methodNode, (mn, generator) -> {
                Type[] argumentTypes = Type.getArgumentTypes(methodNode.signature);
                List<ParameterInfo> parameters = IntStream.range(0, argumentTypes.length).mapToObj(i -> new ParameterInfo(
                    argumentTypes[i].getDescriptor(),
                    ((ParameterNode)methodNode.parameters.get(i)).name
                )).collect(Collectors.toList());
                MethodGenerator methodGenerator = new MethodGenerator(classNode.name, parameters, code);
                methodGenerator.populateMethodBody(mn, generator);

                mn.instructions.add(originalInstruction);
            });
        };*/
    }

    public static MethodNodeExtender addAfter(StatementDom code) {
        return modify((classNode, methodNode, generator, originalInstructions) -> {
            methodNode.instructions.add(originalInstructions);
            add(classNode, methodNode, generator, code);
        });

        /*return (classNode, methodNode) -> {
            InsnList originalInstruction = new InsnList();
            originalInstruction.add(methodNode.instructions);
            methodNode.instructions.clear();

            MethodGenerator.generate(methodNode, (mn, generator) -> {
                Type[] argumentTypes = Type.getArgumentTypes(methodNode.signature);
                List<ParameterInfo> parameters = IntStream.range(0, argumentTypes.length).mapToObj(i -> new ParameterInfo(
                    argumentTypes[i].getDescriptor(),
                    ((ParameterNode)methodNode.parameters.get(i)).name
                )).collect(Collectors.toList());
                MethodGenerator methodGenerator = new MethodGenerator(classNode.name, parameters, code);
                methodGenerator.populateMethodBody(mn, generator);

                mn.instructions.add(originalInstruction);
            });
        };*/
    }

    public static void add(ClassNode classNode, MethodNode methodNode, GeneratorAdapter generator, StatementDom code) {
        Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
        List<ParameterInfo> parameters = IntStream.range(0, argumentTypes.length).mapToObj(i -> new ParameterInfo(
            argumentTypes[i].getDescriptor(),
            methodNode.parameters != null ? ((ParameterNode)methodNode.parameters.get(i)).name : "arg" + i
        )).collect(Collectors.toList());
        MethodGenerator methodGenerator = new MethodGenerator(classNode.name, parameters, code);
        methodGenerator.populateMethodBody(methodNode, generator);
    }
}

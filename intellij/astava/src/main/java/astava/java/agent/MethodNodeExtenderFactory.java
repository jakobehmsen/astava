package astava.java.agent;

import astava.java.gen.MethodGenerator;
import astava.tree.ParameterInfo;
import astava.tree.StatementDom;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.ParameterNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MethodNodeExtenderFactory {
    public static MethodNodeExtender sequence(MethodNodeExtender... extenders) {
        return (classNode, methodNode) -> {
            Arrays.asList(extenders).forEach(x -> x.transform(classNode, methodNode));
        };
    }

    public static MethodNodeExtender setBody(StatementDom replacement) {
        return (classNode, methodNode) -> {
            InsnList originalInstructions = new InsnList();
            originalInstructions.add(methodNode.instructions);
            methodNode.instructions.clear();

            MethodGenerator.generate(methodNode, (mn, generator) -> {
                Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
                List<ParameterInfo> parameters = IntStream.range(0, argumentTypes.length).mapToObj(i -> new ParameterInfo(
                    argumentTypes[i].getDescriptor(),
                    methodNode.parameters != null ? ((ParameterNode)methodNode.parameters.get(i)).name : "arg" + i
                )).collect(Collectors.toList());
                MethodGenerator methodGenerator = new MethodGenerator(classNode.name, parameters, replacement);
                methodGenerator.populateMethodBody(methodNode, originalInstructions, generator);
            });
        };
    }
}

package astava.java.agent;

import astava.java.gen.ByteCodeToTree;
import astava.java.gen.MethodGenerator;
import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import astava.tree.CodeDom;
import astava.tree.ParameterInfo;
import astava.tree.StatementDom;
import astava.tree.Util;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface DeclaringClassNodeExtenderElementBodyNodePredicate {
    default DeclaringMethodNodeExtenderElement then(DeclaringBodyNodeExtenderElement element) {
        return (classNode, thisClass, classResolver, methodNode) -> {
            ArrayList<Object> captures = new ArrayList<>();

            /*Textifier textifier = new Textifier();
            methodNode.accept(new TraceMethodVisitor(textifier));
            textifier.getText().forEach(x -> System.out.print(x));*/

            ByteCodeToTree byteCodeToTree = new ByteCodeToTree(methodNode);
            methodNode.instructions.accept(byteCodeToTree);
            StatementDom body = byteCodeToTree.getBlock();

            return new DeclaringMethodNodeExtenderTransformer() {
                @Override
                public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode, GeneratorAdapter generator, InsnList originalInstructions) {
                    CodeDom replacement = Util.<CodeDom>map(body, (traverser, dom) -> {
                        //return map((BiFunction<Function<CodeDom, CodeDom>, CodeDom, CodeDom>)traverser, dom, captures);
                        if (test(classNode, thisClass, classResolver, methodNode, dom, captures)) {
                            return element.map(classNode, thisClass, classResolver, methodNode, dom, captures);
                        } else
                            return traverser.apply(dom);
                    });
                    replacement.toString();

                    Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
                    List<ParameterInfo> parameters = IntStream.range(0, argumentTypes.length).mapToObj(i -> new ParameterInfo(
                        argumentTypes[i].getDescriptor(),
                        methodNode.parameters != null ? ((ParameterNode)methodNode.parameters.get(i)).name : "arg" + i
                    )).collect(Collectors.toList());
                    MethodGenerator methodGenerator = new MethodGenerator(classNode.name, parameters, (StatementDom)replacement);
                    methodGenerator.populateMethodBody(methodNode, originalInstructions, generator);
                }
            };

            /*return bodyBuilder.test(body, captures);

            if(this.test(classNode, thisClass, classResolver, methodNode, captures)) {
                DeclaringBodyNodeExtenderElementTransformer transformer = element.declare(classNode, thisClass, classResolver, methodNode, captures);

                return new DeclaringMethodNodeExtenderTransformer() {
                    @Override
                    public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode, GeneratorAdapter generator, InsnList originalInstructions) {
                        transformer.transform(classNode, thisClass, classResolver, classInspector, methodNode, generator, originalInstructions, captures);
                    }
                };
            }

            return (classNode1, thisClass1, classResolver1, classInspector, methodNode1, generator, originalInstructions) -> {

            };*/
        };
    }
    boolean test(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode, CodeDom dom, List<Object> captures);
    //CodeDom map(BiFunction<Function<CodeDom, CodeDom>, CodeDom, CodeDom> traverser, CodeDom dom, List<Object> captures);
}

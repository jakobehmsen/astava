package astava.java.agent;

import astava.java.Descriptor;
import astava.java.gen.MethodGenerator;
import astava.java.parser.*;
import astava.tree.ParameterInfo;
import astava.tree.StatementDom;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.ListIterator;

public class MethodNodeExtenderFactory {
    public static DeclaringMethodNodeExtenderTransformer sequence(DeclaringMethodNodeExtenderTransformer... extenders) {
        return (classNode, thisClass, classResolver, classInspector, methodNode, g, originalInstructions) -> {
            Arrays.asList(extenders).forEach(x -> x.transform(classNode, thisClass, classResolver, classInspector, methodNode, g, originalInstructions));
        };
    }

    public static DeclaringMethodNodeExtenderTransformer setBody(StatementDom replacement) {
        return (classNode, thisClass, classResolver, classInspector, methodNode, g, originalInstructions) -> {
            /*InsnList originalInstructions = new InsnList();
            originalInstructions.add(methodNode.instructions);
            methodNode.instructions.clear();*/

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

    public static DeclaringMethodNodeExtenderTransformer append(StatementDom statement) {
        return (classNode, thisClass, classResolver, classInspector, methodNode, g, originalInstructions) -> {
            /*InsnList originalInstructions = new InsnList();
            originalInstructions.add(methodNode.instructions);

            ListIterator it = originalInstructions.iterator();

            Label returnLabel = new Label();

            while(it.hasNext()) {
                AbstractInsnNode insn = (AbstractInsnNode)it.next();

                if(insn.getOpcode()== Opcodes.IRETURN
                    ||insn.getOpcode()==Opcodes.RETURN
                    ||insn.getOpcode()==Opcodes.ARETURN
                    ||insn.getOpcode()==Opcodes.LRETURN
                    ||insn.getOpcode()==Opcodes.DRETURN) {
                    originalInstructions.set();
                }

                originalInstructions.insertBefore();
            }

            methodNode.instructions.clear();*/



            MethodGenerator.generate(methodNode, (mn, generator) -> {
                System.out.println("Append:");
                System.out.println("Class name: " + classNode.name);
                System.out.println("Method name: " + methodNode.name);
                System.out.println("Method parameter count: " + (methodNode.parameters != null ? methodNode.parameters.size() : 0));
                System.out.println("Before:");
                Printer printer=new Textifier();
                mn.accept(new TraceMethodVisitor(printer));
                printer.getText().forEach(x -> System.out.print(x.toString()));

                Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
                List<ParameterInfo> parameters = IntStream.range(0, argumentTypes.length).mapToObj(i -> new ParameterInfo(
                    argumentTypes[i].getDescriptor(),
                    methodNode.parameters != null ? ((ParameterNode)methodNode.parameters.get(i)).name : "arg" + i
                )).collect(Collectors.toList());

                /*InsnList originalInstructions = new InsnList();
                originalInstructions.add(methodNode.instructions);
                methodNode.instructions.clear();*/

                ListIterator it = originalInstructions.iterator();

                Label returnLabel = new Label();

                while(it.hasNext()) {
                    AbstractInsnNode insn = (AbstractInsnNode)it.next();

                    if(insn.getOpcode()== Opcodes.IRETURN
                        ||insn.getOpcode()==Opcodes.RETURN
                        ||insn.getOpcode()==Opcodes.ARETURN
                        ||insn.getOpcode()==Opcodes.LRETURN
                        ||insn.getOpcode()==Opcodes.DRETURN) {
                        generator.visitJumpInsn(Opcodes.GOTO, returnLabel);
                    } else {
                        insn.accept(generator);
                    }
                }

                generator.visitLabel(returnLabel);

                MethodGenerator methodGenerator = new MethodGenerator(classNode.name, parameters, statement);
                methodGenerator.populateMethodBody(methodNode, originalInstructions, generator);

                generator.returnValue();

                System.out.println("After:");
                printer=new Textifier();
                mn.accept(new TraceMethodVisitor(printer));
                printer.getText().forEach(x -> System.out.print(x.toString()));
                //generator.ret();
                //generator.visitInsn(Opcodes.ARETURN);
            });
        };
    }

    public static DeclaringMethodNodeExtenderElement append(StatementDomBuilder statementDomBuilder, ClassInspector classInspector) {
        return new DeclaringMethodNodeExtenderElement() {
            @Override
            public DeclaringMethodNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode) {
                Map<String, String> locals = ASMClassDeclaration.getMethod(methodNode).getParameterTypes().stream()
                    .collect(Collectors.toMap(p -> p.getName(), p -> Descriptor.get(p.getTypeName())));
                StatementDom statement = statementDomBuilder.build(classResolver, thisClass, classInspector, locals);
                return MethodNodeExtenderFactory.append(statement);
            }
        };
    }

    public static DeclaringMethodNodeExtenderElement prepend(StatementDomBuilder statementDomBuilder, ClassInspector classInspector) {
        return new DeclaringMethodNodeExtenderElement() {
            @Override
            public DeclaringMethodNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode) {
                Map<String, String> locals = ASMClassDeclaration.getMethod(methodNode).getParameterTypes().stream()
                    .collect(Collectors.toMap(p -> p.getName(), p -> Descriptor.get(p.getTypeName())));
                StatementDom statement = statementDomBuilder.build(classResolver, thisClass, classInspector, locals);
                return MethodNodeExtenderFactory.prepend(statement);
            }
        };
    }

    private static DeclaringMethodNodeExtenderTransformer prepend(StatementDom statement) {
        return (classNode, thisClass, classResolver, classInspector, methodNode, g, originalInstructions) -> {
            MethodGenerator.generate(methodNode, (mn, generator) -> {
                System.out.println("Prepend:");
                System.out.println("Class name: " + classNode.name);
                System.out.println("Method name: " + methodNode.name);
                System.out.println("Method parameter count: " + (methodNode.parameters != null ? methodNode.parameters.size() : 0));
                System.out.println("Before:");
                Printer printer=new Textifier();
                mn.accept(new TraceMethodVisitor(printer));
                printer.getText().forEach(x -> System.out.print(x.toString()));

                Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
                List<ParameterInfo> parameters = IntStream.range(0, argumentTypes.length).mapToObj(i -> new ParameterInfo(
                    argumentTypes[i].getDescriptor(),
                    methodNode.parameters != null ? ((ParameterNode)methodNode.parameters.get(i)).name : "arg" + i
                )).collect(Collectors.toList());

                /*InsnList originalInstructions = new InsnList();
                originalInstructions.add(methodNode.instructions);
                methodNode.instructions.clear();*/

                MethodGenerator methodGenerator = new MethodGenerator(classNode.name, parameters, statement);
                methodGenerator.populateMethodBody(methodNode, originalInstructions, generator);

                originalInstructions.accept(methodNode);

                System.out.println("After:");
                printer=new Textifier();
                mn.accept(new TraceMethodVisitor(printer));
                printer.getText().forEach(x -> System.out.print(x.toString()));
                //generator.ret();
                //generator.visitInsn(Opcodes.ARETURN);
            });
        };
    }
}

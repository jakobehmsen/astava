package astava.java.agent;

import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.util.ListIterator;

public interface DeclaringMethodNodeExtenderElement {
    DeclaringMethodNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode);

    default DeclaringMethodNodeExtenderElement andThen(DeclaringMethodNodeExtenderElement next) {
        return (classNode, thisClass, classResolver, methodNode) ->
            this.declare(classNode, thisClass, classResolver, methodNode)
            .andThen(next.declare(classNode, thisClass, classResolver, methodNode));
    }

    default DeclaringMethodNodeExtenderElement prepend() {
        return (classNode, thisClass, classResolver, methodNode) -> {
            DeclaringMethodNodeExtenderTransformer transformer = this.declare(classNode, thisClass, classResolver, methodNode);

            return new DeclaringMethodNodeExtenderTransformer() {
                @Override
                public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode, GeneratorAdapter generator) {
                    InsnList originalInstructions = new InsnList();
                    originalInstructions.add(methodNode.instructions);
                    methodNode.instructions.clear();

                    transformer.transform(classNode, thisClass, classResolver, classInspector, methodNode, generator);

                    //originalInstructions.accept(methodNode);
                    originalInstructions.accept(generator);
                }
            };
        };
    }

    default DeclaringMethodNodeExtenderElement append() {
        return (classNode, thisClass, classResolver, methodNode) -> {
            DeclaringMethodNodeExtenderTransformer transformer = this.declare(classNode, thisClass, classResolver, methodNode);

            return new DeclaringMethodNodeExtenderTransformer() {
                @Override
                public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode, GeneratorAdapter generator) {
                    InsnList originalInstructions = new InsnList();
                    originalInstructions.add(methodNode.instructions);
                    methodNode.instructions.clear();

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

                    transformer.transform(classNode, thisClass, classResolver, classInspector, methodNode, generator);

                    generator.returnValue();
                }
            };
        };
    }
}

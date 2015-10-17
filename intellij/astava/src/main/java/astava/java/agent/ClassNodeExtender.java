package astava.java.agent;

import astava.java.agent.Parser.ClassNodePredicateParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public interface ClassNodeExtender extends ClassFileTransformer {
    void transform(ClassNode classNode);
    default ClassNodeExtender when(String sourceCode) throws IOException {
        ClassNodePredicateParser predicateParser = new ClassNodePredicateParser();

        predicateParser.add(sourceCode);

        return when(predicateParser);
    }
    default ClassNodeExtender when(ClassNodePredicate condition) {
        return new ConditionalClassNodeExtender(condition, this);
    }
    default byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        cr.accept(classNode, ClassReader.EXPAND_FRAMES);

        System.out.println(classNode.name);

        try {
            this.transform(classNode);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PrintWriter ps = new PrintWriter(os);
        org.objectweb.asm.util.CheckClassAdapter.verify(new ClassReader(classWriter.toByteArray()), true, ps);
        //cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);

        return classWriter.toByteArray();
    }
}

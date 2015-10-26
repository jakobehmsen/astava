package astava.java.agent;

import astava.java.gen.MethodGenerator;
import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public interface DeclaringMethodNodeExtenderTransformer {
    default void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode) {
        MethodGenerator.generate(methodNode, (mn, generator) -> {
            transform(classNode, thisClass, classResolver, classInspector, methodNode, generator);
        });

        Printer printer = new Textifier();
        methodNode.accept(new TraceMethodVisitor(printer));
        printer.getText().forEach(x -> System.out.print(x.toString()));

        /*methodNode.visitCode();

        Method m = new Method(methodNode.name, methodNode.desc);
        GeneratorAdapter generator;
        try {
            generator = new GeneratorAdapter(methodNode.access, m, methodNode);
        } catch(Exception e) {
            generator = null;
        }

        transform(classNode, thisClass, classResolver, classInspector, methodNode, generator);

        methodNode.visitEnd();
        methodNode.visitMaxs(0, 0);*/
    }
    void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode, GeneratorAdapter generator);
    default DeclaringClassNodeExtenderTransformer when(DeclaringClassNodeExtenderElementMethodNodePredicate condition) {
        return new ConditionalDeclaringMethodNodeExtenderTransformer(condition, this);
    }

    default DeclaringMethodNodeExtenderTransformer andThen(DeclaringMethodNodeExtenderTransformer next) {
        return (classNode, thisClass, classResolver, classInspector, methodNode, generator) -> {
            this.transform(classNode, thisClass, classResolver, classInspector, methodNode, generator);
            next.transform(classNode, thisClass, classResolver, classInspector, methodNode, generator);
        };
    }
}

package astava.java.agent;

import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public interface DeclaringBodyNodeExtenderElementTransformer {
    void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode, GeneratorAdapter generator, InsnList originalInstructions, List<Object> captures);

    default DeclaringBodyNodeExtenderElementTransformer andThen(DeclaringBodyNodeExtenderElementTransformer next) {
        return (classNode, thisClass, classResolver, classInspector, methodNode, generator, originalInstructions, captures) -> {
            this.transform(classNode, thisClass, classResolver, classInspector, methodNode, generator, originalInstructions, captures);
            next.transform(classNode, thisClass, classResolver, classInspector, methodNode, generator, originalInstructions, captures);
        };
    }
}

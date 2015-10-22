package astava.java.agent;

import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface ExDeclaringMethodNodeExtenderTransformer {
    void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode);
    default ExDeclaringClassNodeExtenderTransformer when(ExDeclaringClassNodeExtenderElementMethodNodePredicate condition) {
        return new ExConditionalExDeclaringMethodNodeExtenderTransformer(condition, this);
    }
}

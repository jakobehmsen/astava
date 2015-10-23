package astava.java.agent;

import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface DeclaringMethodNodeExtenderTransformer {
    void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode);
    default DeclaringClassNodeExtenderTransformer when(DeclaringClassNodeExtenderElementMethodNodePredicate condition) {
        return new ConditionalDeclaringMethodNodeExtenderTransformer(condition, this);
    }
}

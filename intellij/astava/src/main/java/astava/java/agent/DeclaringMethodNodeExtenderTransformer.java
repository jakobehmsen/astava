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

    default DeclaringMethodNodeExtenderTransformer andThen(DeclaringMethodNodeExtenderTransformer next) {
        return (classNode, thisClass, classResolver, classInspector, methodNode) -> {
            this.transform(classNode, thisClass, classResolver, classInspector, methodNode);
            next.transform(classNode, thisClass, classResolver, classInspector, methodNode);
        };
    }
}

package astava.java.agent;

import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface DeclaringMethodNodeExtenderElement {
    DeclaringMethodNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode);

    default DeclaringMethodNodeExtenderElement andThen(DeclaringMethodNodeExtenderElement next) {
        return (classNode, thisClass, classResolver, methodNode) ->
            this.declare(classNode, thisClass, classResolver, methodNode)
            .andThen(next.declare(classNode, thisClass, classResolver, methodNode));
    }
}

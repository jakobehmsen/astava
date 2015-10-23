package astava.java.agent;

import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface DeclaringMethodNodeExtenderElement {
    DeclaringClassNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode);
}

package astava.java.agent;

import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;

public interface DeclaringClassNodeExtenderElementPredicate {
    boolean test(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver);

    default DeclaringClassNodeExtenderElementPredicate and(DeclaringClassNodeExtenderElementPredicate next) {
        DeclaringClassNodeExtenderElementPredicate self = this;

        return (classNode, thisClass, classResolver) ->
            self.test(classNode, thisClass, classResolver) && next.test(classNode, thisClass, classResolver);
    }

    default DeclaringClassNodeExtenderElement then(DeclaringClassNodeExtenderElement element) {
        return new ConditionalDeclaringClassNodeExtenderElement(this, element);
    }

    default DeclaringClassNodeExtenderElementPredicate not() {
        return (classNode, thisClass, classResolver) ->
            !test(classNode, thisClass, classResolver);
    }
}

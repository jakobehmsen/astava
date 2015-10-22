package astava.java.agent;

import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;

public interface ExDeclaringClassNodeExtenderElementPredicate {
    boolean test(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver);

    default ExDeclaringClassNodeExtenderElementPredicate and(ExDeclaringClassNodeExtenderElementPredicate next) {
        ExDeclaringClassNodeExtenderElementPredicate self = this;

        return (classNode, thisClass, classResolver) ->
            self.test(classNode, thisClass, classResolver) && next.test(classNode, thisClass, classResolver);
    }

    default ExDeclaringClassNodeExtenderElement then(ExDeclaringClassNodeExtenderElement element) {
        return new ExConditionalExDeclaringClassNodeExtenderElement(this, element);
    }
}

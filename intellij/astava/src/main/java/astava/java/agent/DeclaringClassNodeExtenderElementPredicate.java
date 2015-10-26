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
        return (classNode, thisClass, classResolver) -> {
            if(this.test(classNode, thisClass, classResolver))
                return element.declare(classNode, thisClass, classResolver);

            return (classNode1, thisClass1, classResolver1, classInspector) -> { };
        };
    }

    default DeclaringClassNodeExtenderElementPredicate not() {
        return (classNode, thisClass, classResolver) ->
            !test(classNode, thisClass, classResolver);
    }
}

package astava.java.agent;

import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;

public class ConditionalDeclaringClassNodeExtenderElement implements DeclaringClassNodeExtenderElement {
    private DeclaringClassNodeExtenderElementPredicate predicate;
    private DeclaringClassNodeExtenderElement element;

    public ConditionalDeclaringClassNodeExtenderElement(DeclaringClassNodeExtenderElementPredicate predicate, DeclaringClassNodeExtenderElement element) {
        this.predicate = predicate;
        this.element = element;
    }

    @Override
    public DeclaringClassNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver) {
        if(predicate.test(classNode, thisClass, classResolver))
            return element.declare(classNode, thisClass, classResolver);

        return (classNode1, thisClass1, classResolver1, classInspector) -> {

        };
    }
}

package astava.java.agent;

import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;

public class ExConditionalExDeclaringClassNodeExtenderElement implements ExDeclaringClassNodeExtenderElement {
    private ExDeclaringClassNodeExtenderElementPredicate predicate;
    private ExDeclaringClassNodeExtenderElement element;

    public ExConditionalExDeclaringClassNodeExtenderElement(ExDeclaringClassNodeExtenderElementPredicate predicate, ExDeclaringClassNodeExtenderElement element) {
        this.predicate = predicate;
        this.element = element;
    }

    @Override
    public ExDeclaringClassNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver) {
        if(predicate.test(classNode, thisClass, classResolver))
            return element.declare(classNode, thisClass, classResolver);

        return (classNode1, thisClass1, classResolver1, classInspector) -> {

        };
    }
}

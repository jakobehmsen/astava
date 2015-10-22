package astava.java.agent;

import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class ExDeclaringClassNodeExtenderUtil {
    public static ExDeclaringClassNodeExtenderElement composeElement(List<ExDeclaringClassNodeExtenderElement> elements) {
        return elements.stream()
            .reduce((x, y) -> x.andThen(y))
            .orElse((classNode, thisClass, classResolver) -> null);
    }

    public static ExDeclaringClassNodeExtenderElementPredicate composePredicate(List<ExDeclaringClassNodeExtenderElementPredicate> elements) {
        return elements.stream()
            .reduce((x, y) -> x.and(y))
            .orElse((classNode, thisClass, classResolver) -> false);
    }
}

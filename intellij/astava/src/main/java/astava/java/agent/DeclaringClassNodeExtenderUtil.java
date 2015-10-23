package astava.java.agent;

import java.util.List;

public class DeclaringClassNodeExtenderUtil {
    public static DeclaringClassNodeExtenderElement composeElement(List<DeclaringClassNodeExtenderElement> elements) {
        return elements.stream()
            .reduce((x, y) -> x.andThen(y))
            .orElse((classNode, thisClass, classResolver) -> null);
    }

    public static DeclaringClassNodeExtenderElementPredicate composePredicate(List<DeclaringClassNodeExtenderElementPredicate> elements) {
        return elements.stream()
            .reduce((x, y) -> x.and(y))
            .orElse((classNode, thisClass, classResolver) -> false);
    }
}

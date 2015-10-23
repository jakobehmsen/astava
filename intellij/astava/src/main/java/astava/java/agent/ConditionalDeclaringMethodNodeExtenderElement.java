package astava.java.agent;

import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.stream.Collectors;

public class ConditionalDeclaringMethodNodeExtenderElement implements DeclaringClassNodeExtenderElement {
    private DeclaringClassNodeExtenderElementMethodNodePredicate predicate;
    private DeclaringMethodNodeExtenderElement element;

    public ConditionalDeclaringMethodNodeExtenderElement(DeclaringClassNodeExtenderElementMethodNodePredicate predicate, DeclaringMethodNodeExtenderElement element) {
        this.predicate = predicate;
        this.element = element;
    }

    @Override
    public DeclaringClassNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver) {
        List<DeclaringClassNodeExtenderTransformer> transformers = ((List<MethodNode>)classNode.methods).stream()
            .filter(m ->
                predicate.test(classNode, thisClass, classResolver, m))
            .map(m -> new DeclaringClassNodeExtenderTransformer() {
                DeclaringMethodNodeExtenderTransformer transformer = element.declare(classNode, thisClass, classResolver, m);

                @Override
                public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector) {
                    transformer.transform(classNode, thisClass, classResolver, classInspector, m);
                }
            })
            .collect(Collectors.toList());

        return (classNode1, thisClass1, classResolver1, classInspector) -> {
            transformers.forEach(t -> t.transform(classNode1, thisClass1, classResolver1, classInspector));
        };
    }
}

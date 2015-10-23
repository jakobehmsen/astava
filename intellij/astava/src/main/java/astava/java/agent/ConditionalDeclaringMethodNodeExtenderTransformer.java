package astava.java.agent;

import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class ConditionalDeclaringMethodNodeExtenderTransformer implements DeclaringClassNodeExtenderTransformer {
    private DeclaringClassNodeExtenderElementMethodNodePredicate predicate;
    private DeclaringMethodNodeExtenderTransformer transformer;

    public ConditionalDeclaringMethodNodeExtenderTransformer(DeclaringClassNodeExtenderElementMethodNodePredicate predicate, DeclaringMethodNodeExtenderTransformer transformer) {
        this.predicate = predicate;
        this.transformer = transformer;
    }

    @Override
    public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector) {
        ((List<MethodNode>)classNode.methods).stream()
            .filter(m -> predicate.test(classNode, thisClass, classResolver, m))
            .forEach(m -> transformer.transform(classNode, thisClass, classResolver, classInspector, m));
    }
}

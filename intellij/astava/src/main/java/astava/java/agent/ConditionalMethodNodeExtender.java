package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.BiPredicate;

public class ConditionalMethodNodeExtender implements ClassNodeExtender {
    private BiPredicate<ClassNode, MethodNode> condition;
    private MethodNodeExtender extender;

    public ConditionalMethodNodeExtender(BiPredicate<ClassNode, MethodNode> condition, MethodNodeExtender extender) {
        this.condition = condition;
        this.extender = extender;
    }

    @Override
    public void transform(ClassNode classNode) {
        classNode.methods.stream()
            .filter(x -> condition.test(classNode, (MethodNode)x))
            .forEach(x -> extender.transform(classNode, (MethodNode)x));
    }
}
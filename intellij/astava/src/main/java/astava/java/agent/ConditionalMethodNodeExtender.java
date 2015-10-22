package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.BiPredicate;

public class ConditionalMethodNodeExtender implements ClassNodeExtender {
    private MethodNodePredicate condition;
    private MethodNodeExtender extender;

    public ConditionalMethodNodeExtender(MethodNodePredicate condition, MethodNodeExtender extender) {
        this.condition = condition;
        this.extender = extender;
    }

    @Override
    public void transform(ClassNode classNode) {
        for (Object x: classNode.methods) {
            if(condition.test(classNode, (MethodNode)x))
                extender.transform(classNode, (MethodNode)x);
        }
        /*classNode.methods.stream()
            .filter(x ->
                condition.test(classNode, (MethodNode)x))
            .forEach(x ->
                extender.transform(classNode, (MethodNode)x));*/
    }
}

package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.BiPredicate;

public interface MethodNodeExtender {
    void transform(ClassNode classNode, MethodNode methodNode);
    default ClassNodeExtender when(MethodNodePredicate condition) {
        return new ConditionalMethodNodeExtender(condition, this);
    }
}

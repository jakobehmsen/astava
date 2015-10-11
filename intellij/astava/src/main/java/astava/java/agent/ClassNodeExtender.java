package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;

import java.util.function.Predicate;

public interface ClassNodeExtender {
    void transform(ClassNode classNode);
    default ClassNodeExtender when(Predicate<ClassNode> condition) {
        return new ConditionalClassNodeExtender(condition, this);
    }
}

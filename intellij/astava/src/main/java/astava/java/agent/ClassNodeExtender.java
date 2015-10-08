package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;

public interface ClassNodeExtender {
    void transform(ClassNode classNode);
}

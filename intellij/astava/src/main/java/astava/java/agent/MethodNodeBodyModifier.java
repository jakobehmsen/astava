package astava.java.agent;

import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by jakob on 09-10-15.
 */
public interface MethodNodeBodyModifier {
    void modify(ClassNode classNode, MethodNode methodNode, GeneratorAdapter generator, InsnList originalInstructions);
}

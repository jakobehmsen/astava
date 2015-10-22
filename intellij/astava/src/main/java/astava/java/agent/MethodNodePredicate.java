package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.BiPredicate;

public interface MethodNodePredicate extends BiPredicate<ClassNode, MethodNode> {
}

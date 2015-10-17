package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;

import java.util.function.Predicate;

public interface ClassNodePredicate extends Predicate<ClassNode> {
}

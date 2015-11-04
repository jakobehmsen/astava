package astava.java.agent;

import astava.java.parser.ClassResolver;
import org.objectweb.asm.tree.ClassNode;

import java.util.function.BiPredicate;

public interface ClassNodePredicate extends BiPredicate<ClassNode, ClassResolver> {
}

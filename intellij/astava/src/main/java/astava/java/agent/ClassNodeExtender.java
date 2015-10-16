package astava.java.agent;

import astava.java.parser.Parser;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.function.Predicate;

public interface ClassNodeExtender {
    void transform(ClassNode classNode);
    default ClassNodeExtender when(String sourceCode) throws IOException {
        return when(new Parser(sourceCode).parseClassPredicate());
    }
    default ClassNodeExtender when(Predicate<ClassNode> condition) {
        return new ConditionalClassNodeExtender(condition, this);
    }
}

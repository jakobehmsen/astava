package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.*;
import java.util.function.Predicate;

public class ConditionalClassNodeExtender implements ClassNodeExtender {
    private Predicate<ClassNode> condition;
    private ClassNodeExtender extender;

    public ConditionalClassNodeExtender(Predicate<ClassNode> condition, ClassNodeExtender extender) {
        this.condition = condition;
        this.extender = extender;
    }

    @Override
    public void transform(ClassNode classNode) {
        if(condition.test(classNode))
            extender.transform(classNode);
    }
}

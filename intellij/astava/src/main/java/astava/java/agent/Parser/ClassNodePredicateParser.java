package astava.java.agent.Parser;

import astava.java.agent.ClassNodePredicate;
import astava.java.parser.ClassInspector;
import astava.java.parser.Parser;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;

public class ClassNodePredicateParser implements ClassNodePredicate {
    private ClassInspector classInspector;
    private ArrayList<ClassNodePredicate> predicates = new ArrayList<>();

    public ClassNodePredicateParser(ClassInspector classInspector) {
        this.classInspector = classInspector;
    }

    public ClassNodePredicateParser add(String sourceCode) throws IOException {
        Parser parser = new Parser(sourceCode);

        predicates.addAll(parser.parseClassPredicates(classInspector));

        return this;
    }

    @Override
    public boolean test(ClassNode classNode) {
        return predicates.stream().allMatch(x -> x.test(classNode));
    }
}

package astava.java.agent.Parser;

import astava.java.agent.ClassNodePredicate;
import astava.java.parser.Parser;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;

public class ClassNodePredicateParser implements ClassNodePredicate {
    private ArrayList<ClassNodePredicate> predicates = new ArrayList<>();

    public void add(String sourceCode) throws IOException {
        Parser parser = new Parser(sourceCode);

        predicates.addAll(parser.parseClassPredicates());
    }

    @Override
    public boolean test(ClassNode classNode) {
        return predicates.stream().allMatch(x -> x.test(classNode));
    }
}

package astava.java.agent.Parser;

import astava.java.agent.ClassNodeExtender;
import astava.java.agent.ClassNodePredicate;
import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import org.objectweb.asm.tree.ClassNode;

public class ParserFactory {
    private ClassResolver classResolver;
    private ClassInspector classInspector;

    public ParserFactory(ClassResolver classResolver, ClassInspector classInspector) {
        this.classResolver = classResolver;
        this.classInspector = classInspector;
    }

    public ClassNodeExtenderParser newExtender() {
        return new ClassNodeExtenderParser(classResolver, classInspector);
    }

    public ClassNodePredicateParser newPredicate() {
        return new ClassNodePredicateParser(classInspector);
    }
}

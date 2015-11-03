package astava.java.agent;

import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.tree.ClassNode;

public interface DeclaringClassNodeExtenderElementPredicate {
    boolean test(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver);

    default DeclaringClassNodeExtenderElementPredicate and(DeclaringClassNodeExtenderElementPredicate next) {
        DeclaringClassNodeExtenderElementPredicate self = this;

        return (classNode, thisClass, classResolver) ->
            self.test(classNode, thisClass, classResolver) && next.test(classNode, thisClass, classResolver);
    }

    default DeclaringClassNodeExtenderElement then(DeclaringClassNodeExtenderElement element) {
        return (classNode, thisClass, classResolver) -> {
            if(this.test(classNode, thisClass, classResolver)) {
                DeclaringClassNodeExtenderTransformer transformer = element.declare(classNode, thisClass, classResolver);

                return new DeclaringClassNodeExtenderTransformer() {
                    @Override
                    public boolean willTransform() {
                        return true;
                    }

                    @Override
                    public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector) {
                        transformer.transform(classNode, thisClass, classResolver, classInspector);
                    }
                };
            }

            return new DeclaringClassNodeExtenderTransformer() {
                @Override
                public boolean willTransform() {
                    return false;
                }

                @Override
                public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector) {

                }
            };
        };
    }

    default DeclaringClassNodeExtenderElementPredicate not() {
        return (classNode, thisClass, classResolver) ->
            !test(classNode, thisClass, classResolver);
    }
}

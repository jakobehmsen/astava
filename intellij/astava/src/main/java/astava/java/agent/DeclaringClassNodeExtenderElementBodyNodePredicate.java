package astava.java.agent;

import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public interface DeclaringClassNodeExtenderElementBodyNodePredicate {
    default DeclaringMethodNodeExtenderElement then(DeclaringBodyNodeExtenderElement element) {
        return (classNode, thisClass, classResolver, methodNode) -> {
            ArrayList<Object> captures = new ArrayList<>();
            if(this.test(classNode, thisClass, classResolver, methodNode, captures)) {
                DeclaringBodyNodeExtenderElementTransformer transformer = element.declare(classNode, thisClass, classResolver, methodNode, captures);

                return new DeclaringMethodNodeExtenderTransformer() {
                    @Override
                    public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode, GeneratorAdapter generator, InsnList originalInstructions) {
                        transformer.transform(classNode, thisClass, classResolver, classInspector, methodNode, generator, originalInstructions, captures);
                    }
                };
            }

            return (classNode1, thisClass1, classResolver1, classInspector, methodNode1, generator, originalInstructions) -> {

            };
        };
    }
    boolean test(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode, List<Object> captures);
}

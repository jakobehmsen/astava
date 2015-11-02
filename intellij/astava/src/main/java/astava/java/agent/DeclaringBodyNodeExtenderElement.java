package astava.java.agent;

import astava.java.parser.ClassResolver;
import astava.java.parser.MutableClassDeclaration;
import astava.tree.CodeDom;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface DeclaringBodyNodeExtenderElement {
    default DeclaringBodyNodeExtenderElement andThen(DeclaringBodyNodeExtenderElement next) {
        /*return (classNode, thisClass, classResolver, methodNode, captures) ->
            this.declare(classNode, thisClass, classResolver, methodNode, captures)
                .andThen(next.declare(classNode, thisClass, classResolver, methodNode, captures));*/

        return null;
    }

    //DeclaringBodyNodeExtenderElementTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode, List<Object> captures);
    CodeDom map(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode, CodeDom dom, List<Object> captures);
    //void transform();
}

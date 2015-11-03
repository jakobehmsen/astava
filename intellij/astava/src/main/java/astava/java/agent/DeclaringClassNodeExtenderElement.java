package astava.java.agent;

import astava.java.Descriptor;
import astava.java.parser.*;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public interface DeclaringClassNodeExtenderElement extends ClassNodeExtender {
    DeclaringClassNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver);

    default DeclaringClassNodeExtenderElement andThen(DeclaringClassNodeExtenderElement next) {
        DeclaringClassNodeExtenderElement self = this;

        return (classNode, thisClass, classResolver) -> {
            DeclaringClassNodeExtenderTransformer thisTransformer = self.declare(classNode, thisClass, classResolver);
            DeclaringClassNodeExtenderTransformer nextTransformer = next.declare(classNode, thisClass, classResolver);

            return (classNode1, thisClass1, classResolver1, classInspector) -> {
                thisTransformer.transform(classNode1, thisClass1, classResolver1, classInspector);
                nextTransformer.transform(classNode1, thisClass1, classResolver1, classInspector);
            };
        };
    }

    default boolean transform(ClassNode classNode, ClassResolver classResolver, ClassInspector classInspector) {
        MutableClassDeclaration thisClass = new MutableClassDeclaration();

        thisClass.setName(Descriptor.getName(classNode.name));
        thisClass.setSuperName(classNode.superName != null ? Descriptor.getName(classNode.superName) : null);

        // Include all fields and methods (members in general) of classNode
        ((List<String>)classNode.interfaces).forEach(x -> thisClass.addInterface(Descriptor.getName(x)));
        ASMClassDeclaration.getFields(classNode).forEach(x -> thisClass.addField(x));
        ASMClassDeclaration.getMethods(classNode).forEach(x -> thisClass.addMethod(x));

        DeclaringClassNodeExtenderTransformer transformer = this.declare(classNode, thisClass, classResolver);

        if(transformer.willTransform()) {
            ClassInspector classInspectorForThis = new ClassInspector() {
                @Override
                public ClassDeclaration getClassDeclaration(String name) {
                    //if(Descriptor.get(name).equals(thisClass.getName()))
                    if (name.equals(thisClass.getName()))
                        return thisClass;
                    return classInspector.getClassDeclaration(name);
                }
            };

            transformer.transform(classNode, thisClass, classResolver, classInspectorForThis);

            return true;
        }

        return false;
    }

    default DeclaringClassNodeExtenderElement when(DeclaringClassNodeExtenderElementPredicate predicate) {
        return predicate.then(this);
    }
}

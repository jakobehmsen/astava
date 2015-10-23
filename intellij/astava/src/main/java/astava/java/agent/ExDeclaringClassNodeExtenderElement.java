package astava.java.agent;

import astava.java.Descriptor;
import astava.java.parser.*;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public interface ExDeclaringClassNodeExtenderElement extends ExClassNodeExtender {
    ExDeclaringClassNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver);

    default ExDeclaringClassNodeExtenderElement andThen(ExDeclaringClassNodeExtenderElement next) {
        ExDeclaringClassNodeExtenderElement self = this;

        return (classNode, thisClass, classResolver) -> {
            ExDeclaringClassNodeExtenderTransformer thisTransformer = self.declare(classNode, thisClass, classResolver);
            ExDeclaringClassNodeExtenderTransformer nextTransformer = next.declare(classNode, thisClass, classResolver);

            return (classNode1, thisClass1, classResolver1, classInspector) -> {
                thisTransformer.transform(classNode1, thisClass1, classResolver1, classInspector);
                nextTransformer.transform(classNode1, thisClass1, classResolver1, classInspector);
            };
        };
    }

    default void transform(ClassNode classNode, ClassResolver classResolver, ClassInspector classInspector) {
        MutableClassDeclaration thisClass = new MutableClassDeclaration();

        thisClass.setName(Descriptor.getName(classNode.name));
        thisClass.setSuperName(classNode.superName != null ? Descriptor.getName(classNode.superName) : null);

        // Include all fields and methods (members in general) of classNode
        ((List<String>)classNode.interfaces).forEach(x -> thisClass.addInterface(Descriptor.getName(x)));
        ASMClassDeclaration.getFields(classNode).forEach(x -> thisClass.addField(x));
        ASMClassDeclaration.getMethods(classNode).forEach(x -> thisClass.addMethod(x));

        ExDeclaringClassNodeExtenderTransformer transformer = this.declare(classNode, thisClass, classResolver);

        ClassInspector classInspectorForThis = new ClassInspector() {
            @Override
            public ClassDeclaration getClassDeclaration(String name) {
                if(Descriptor.get(name).equals(thisClass.getName()))
                    return thisClass;
                return classInspector.getClassDeclaration(name);
            }
        };

        transformer.transform(classNode, thisClass, classResolver, classInspectorForThis);
    }

    default ExDeclaringClassNodeExtenderElement when(ExDeclaringClassNodeExtenderElementPredicate predicate) {
        return new ExConditionalExDeclaringClassNodeExtenderElement(predicate, this);
    }
}

package astava.java.agent;

import astava.java.Descriptor;
import astava.java.parser.*;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class ExDeclaringClassNodeExtender implements ExClassNodeExtender {
    private ExDeclaringClassNodeExtenderElement element;

    public ExDeclaringClassNodeExtender(ExDeclaringClassNodeExtenderElement element) {
        this.element = element;
    }

    @Override
    public void transform(ClassNode classNode, ClassResolver classResolver, ClassInspector classInspector) {
        MutableClassDeclaration thisClass = new MutableClassDeclaration();

        thisClass.setName(classNode.name);
        thisClass.setSuperName(classNode.superName);

        // Include all fields and methods (members in general) of classNode
        ((List<String>)classNode.interfaces).forEach(x -> thisClass.addInterface(Descriptor.getName(x)));
        ASMClassDeclaration.getFields(classNode).forEach(x -> thisClass.addField(x));
        ASMClassDeclaration.getMethods(classNode).forEach(x -> thisClass.addMethod(x));

        ExDeclaringClassNodeExtenderTransformer transformer = element.declare(classNode, thisClass, classResolver);

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
}

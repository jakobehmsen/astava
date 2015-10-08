package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;

public class SequenceClassNodeExtender implements ClassNodeExtender {
    private ArrayList<ClassNodeExtender> extenders = new ArrayList<>();

    public void extend(ClassNodeExtender extender) {
        extenders.add(extender);
    }

    @Override
    public void transform(ClassNode classNode) {
        extenders.forEach(x -> x.transform(classNode));
    }
}

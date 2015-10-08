package astava.java.agent;

import org.objectweb.asm.tree.ClassNode;

import java.util.*;
import java.util.function.Predicate;

public class ConditionalClassNodeExtender implements ClassNodeExtender {
    private LinkedHashMap<Predicate<ClassNode>, ClassNodeExtender> extenders = new LinkedHashMap<>();

    public void extend(Predicate<ClassNode> condition, ClassNodeExtender extender) {
        extenders.put(condition, extender);
    }

    @Override
    public void transform(ClassNode classNode) {
        LinkedHashMap<Predicate<ClassNode>, ClassNodeExtender> classInterceptorsCopy = new LinkedHashMap<>(extenders);

        while(true) {
            Optional<Map.Entry<Predicate<ClassNode>, ClassNodeExtender>> entry =
                classInterceptorsCopy.entrySet().stream().filter(x -> x.getKey().test(classNode)).findFirst();
            if(entry.isPresent()) {
                entry.get().getValue().transform(classNode);
                classInterceptorsCopy.remove(entry.get().getKey());
            } else
                break;
        }
    }
}

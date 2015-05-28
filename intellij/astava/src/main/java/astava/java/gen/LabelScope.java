package astava.java.gen;

import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LabelScope {
    private static class LabelInfo {
        public Label label;
        public boolean isSet;
        public boolean isUsed;

        public LabelInfo(Label label) {
            this.label = label;
        }
    }

    private Map<String, LabelInfo> nameToLabelMap = new Hashtable<>();

    public void verify() {
        List<String> usedAndNotSet =
            nameToLabelMap.entrySet().stream().filter(e -> e.getValue().isUsed && !e.getValue().isSet).map(e -> e.getKey()).collect(Collectors.toList());

        if(usedAndNotSet.size() > 0) {
            String usedAndNotSetAsString = usedAndNotSet.stream().collect(Collectors.joining(", "));
            throw new IllegalStateException("Unset labels were used: " + usedAndNotSetAsString + ".");
        }
    }

    public void label(GeneratorAdapter generator, String name) {
        LabelInfo label = getOrCreate(generator, name);
        if(label.isSet)
            throw new IllegalArgumentException("Label '" + name + "' is already set.");
        generator.visitLabel(label.label);
        label.isSet = true;
    }

    public void goTo(GeneratorAdapter generator, String name) {
        LabelInfo label = getOrCreate(generator, name);
        generator.goTo(label.label);
        label.isUsed = true;
    }

    private LabelInfo getOrCreate(GeneratorAdapter generator, String name) {
        return nameToLabelMap.computeIfAbsent(name, k -> new LabelInfo(generator.newLabel()));
    }
}

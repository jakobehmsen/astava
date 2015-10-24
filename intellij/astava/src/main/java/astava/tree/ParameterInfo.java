package astava.tree;

import astava.java.Descriptor;

public class ParameterInfo {
    public final String descriptor;
    public final String name;

    public ParameterInfo(String descriptor, String name) {
        this.descriptor = descriptor;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return Descriptor.getName(descriptor);
    }
}

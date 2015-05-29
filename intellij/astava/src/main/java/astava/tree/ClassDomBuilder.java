package astava.tree;

import java.util.ArrayList;
import java.util.List;

public class ClassDomBuilder implements ClassDom {
    private int modifier;
    private String name;
    private String superName;
    private ArrayList<FieldDom> fields = new ArrayList<>();
    private ArrayList<MethodDom> methods = new ArrayList<>();

    @Override
    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getSuperName() {
        return superName;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    @Override
    public List<FieldDom> getFields() {
        return fields;
    }

    @Override
    public List<MethodDom> getMethods() {
        return methods;
    }

    public void setFrom(ClassDom c) {
        setModifier(c.getModifier());
        setName(c.getName());
        setSuperName(c.getSuperName());
        getFields().retainAll(c.getFields());
        getMethods().retainAll(c.getMethods());
    }

    public void append(ClassDom c) {
        getFields().addAll(c.getFields());
        getMethods().addAll(c.getMethods());
    }
}

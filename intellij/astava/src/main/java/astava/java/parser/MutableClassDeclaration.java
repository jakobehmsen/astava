package astava.java.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

public class MutableClassDeclaration implements ClassDeclaration {
    private int modifier;
    private String name;
    private String superName;
    private boolean isInterface;
    private Hashtable<String, FieldDeclaration> fields = new Hashtable<>();
    private Hashtable<String, MethodDeclaration> methods = new Hashtable<>();
    private ArrayList<String> interfaces = new ArrayList<>();

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public void setIsInterface(boolean isInterface) {
        this.isInterface = isInterface;
    }

    public void addField(FieldDeclaration field) {
        fields.put(field.getName(), field);
    }

    public void addMethod(MethodDeclaration method) {
        methods.put(method.getName(), method);
    }

    @Override
    public List<FieldDeclaration> getFields() {
        return fields.values().stream().collect(Collectors.toList());
    }

    @Override
    public List<MethodDeclaration> getMethods() {
        return methods.values().stream().collect(Collectors.toList());
    }

    @Override
    public int getModifiers() {
        return modifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSuperName() {
        return superName;
    }

    @Override
    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public List<String> getInterfaces() {
        return interfaces;
    }

    public void addInterface(String name) {
        interfaces.add(name);
    }
}

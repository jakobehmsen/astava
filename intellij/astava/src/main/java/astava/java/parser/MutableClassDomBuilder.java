package astava.java.parser;

import astava.tree.ClassDom;
import astava.tree.FieldDom;
import astava.tree.MethodDom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MutableClassDomBuilder implements ClassDomBuilder {
    private int modifier;
    private String name;
    private String superName;
    private ArrayList<MethodDomBuilder> methodBuilders = new ArrayList<>();

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public void addMethod(MethodDomBuilder methodBuilder) {
        methodBuilders.add(methodBuilder);
    }

    @Override
    public ClassDom build(ClassResolver classResolver) {
        ClassDeclaration cd = new ClassDeclaration() {
            @Override
            public List<FieldDeclaration> getFields() {
                return Arrays.asList();
            }
        };

        List<MethodDom> methods = methodBuilders.stream().map(x -> x.build(classResolver, cd)).collect(Collectors.toList());

        return new ClassDom() {
            @Override
            public int getModifier() {
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
            public List<FieldDom> getFields() {
                return Arrays.asList();
            }

            @Override
            public List<MethodDom> getMethods() {
                return methods;
            }
        };
    }
}
